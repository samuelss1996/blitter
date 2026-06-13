#!/usr/bin/env python3
"""
Create Google Play support products from support-products-draft.json.

This writes to Google Play Console using the monetization.onetimeproducts
batchUpdate endpoint with allowMissing=true.
"""

from __future__ import annotations

import argparse
import json
import os
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


ANDROID_PUBLISHER_SCOPE = "https://www.googleapis.com/auth/androidpublisher"
BASE_URL = "https://androidpublisher.googleapis.com/androidpublisher/v3"
DEFAULT_PACKAGE_NAME = "es.soutullo.blitter"
DEFAULT_DRAFT_PATH = "build/reports/play-products/support-products-draft.json"
DEFAULT_OUTPUT_PATH = "build/reports/play-products/support-products-create-result.json"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Create Google Play support products from a reviewed draft JSON."
    )
    parser.add_argument(
        "--draft",
        default=DEFAULT_DRAFT_PATH,
        help=f"Draft JSON path. Defaults to {DEFAULT_DRAFT_PATH}.",
    )
    parser.add_argument(
        "--package-name",
        default=DEFAULT_PACKAGE_NAME,
        help=f"Android package name. Defaults to {DEFAULT_PACKAGE_NAME}.",
    )
    parser.add_argument(
        "--credentials",
        default=os.environ.get("GOOGLE_APPLICATION_CREDENTIALS"),
        help="Path to service account JSON. Defaults to GOOGLE_APPLICATION_CREDENTIALS.",
    )
    parser.add_argument(
        "--output",
        default=DEFAULT_OUTPUT_PATH,
        help=f"Write API response here. Defaults to {DEFAULT_OUTPUT_PATH}.",
    )
    return parser.parse_args()


def create_authorized_session(credentials_path: str):
    try:
        from google.auth.transport.requests import AuthorizedSession
        from google.oauth2 import service_account
    except ImportError as error:
        raise SystemExit(
            "Missing dependencies. Install them with:\n"
            "  python3 -m venv .venv-play-api\n"
            "  source .venv-play-api/bin/activate\n"
            "  pip install -r scripts/google-play-api-requirements.txt"
        ) from error

    credentials = service_account.Credentials.from_service_account_file(
        credentials_path,
        scopes=[ANDROID_PUBLISHER_SCOPE],
    )
    return AuthorizedSession(credentials)


def request_json(session: Any, url: str, body: dict[str, Any]) -> dict[str, Any]:
    response = session.post(url, json=body)

    if response.status_code >= 400:
        error_path = Path(DEFAULT_OUTPUT_PATH).with_name("support-products-create-error.json")
        error_path.parent.mkdir(parents=True, exist_ok=True)
        error_path.write_text(response.text + "\n", encoding="utf-8")
        raise RuntimeError(
            f"POST {response.url} failed with HTTP {response.status_code}. "
            f"Response written to {error_path}"
        )

    return response.json()


def load_draft(path: Path) -> list[dict[str, Any]]:
    payload = json.loads(path.read_text(encoding="utf-8"))
    return payload["products"]


def one_time_product_payload(product: dict[str, Any], package_name: str) -> dict[str, Any]:
    payload = {
        key: value
        for key, value in product.items()
        if key not in ("sourceProductId", "sourceRegionsVersion")
    }
    payload["packageName"] = package_name

    for purchase_option in payload.get("purchaseOptions", []):
        purchase_option.pop("sourcePurchaseOptionId", None)

    return payload


def update_request(product: dict[str, Any], package_name: str) -> dict[str, Any]:
    regions_version = product.get("sourceRegionsVersion")
    if not regions_version:
        raise ValueError(f"Missing sourceRegionsVersion for {product.get('productId')}")

    return {
        "oneTimeProduct": one_time_product_payload(product, package_name),
        "updateMask": "listings,purchaseOptions",
        "regionsVersion": regions_version,
        "allowMissing": True,
    }


def write_output(path: Path, payload: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        json.dumps(payload, ensure_ascii=False, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )


def print_summary(products: list[dict[str, Any]], output_path: Path) -> None:
    print(f"Created/updated {len(products)} support product(s)")
    print(f"Output: {output_path}")
    for product in products:
        print(f"- {product.get('productId')}")


def main() -> int:
    args = parse_args()

    if not args.credentials:
        print(
            "GOOGLE_APPLICATION_CREDENTIALS is not set and --credentials was not provided.",
            file=sys.stderr,
        )
        return 2

    credentials_path = Path(args.credentials).expanduser()
    if not credentials_path.exists():
        print(f"Credentials file does not exist: {credentials_path}", file=sys.stderr)
        return 2

    draft_path = Path(args.draft)
    products = load_draft(draft_path)
    body = {
        "requests": [
            update_request(product, args.package_name)
            for product in products
        ]
    }

    session = create_authorized_session(str(credentials_path))
    response = request_json(
        session,
        f"{BASE_URL}/applications/{args.package_name}/oneTimeProducts:batchUpdate",
        body,
    )

    output_path = Path(args.output)
    result = {
        "createdAt": datetime.now(timezone.utc).isoformat(),
        "draft": str(draft_path),
        "requestProductIds": [product["productId"] for product in products],
        "response": response,
    }
    write_output(output_path, result)
    print_summary(response.get("oneTimeProducts", []), output_path)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
