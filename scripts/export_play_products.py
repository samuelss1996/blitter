#!/usr/bin/env python3
"""
Export Google Play one-time products without modifying Play Console.

Authentication uses a Google service account JSON file via:

    export GOOGLE_APPLICATION_CREDENTIALS=/absolute/path/to/service-account.json

The output is intended for local inspection before creating new support products.
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


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Export Google Play one-time product catalog data."
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
        help="Output JSON path. Defaults to build/reports/play-products/catalog-<timestamp>.json.",
    )
    parser.add_argument(
        "--product-id-contains",
        default="removeads",
        help="Only include products whose ID contains this text. Defaults to removeads.",
    )
    parser.add_argument(
        "--include-all-products",
        action="store_true",
        help="Ignore --product-id-contains and export every product returned by the APIs.",
    )
    parser.add_argument(
        "--api",
        choices=("both", "onetimeproducts", "inappproducts"),
        default="both",
        help="Which catalog API to query. Defaults to both.",
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


def request_json(session: Any, url: str, params: dict[str, Any] | None = None) -> dict[str, Any]:
    response = session.get(url, params=params)

    if response.status_code >= 400:
        body = response.text[:2_000]
        raise RuntimeError(f"GET {response.url} failed with HTTP {response.status_code}: {body}")

    return response.json()


def list_onetime_products(session: Any, package_name: str) -> list[dict[str, Any]]:
    products: list[dict[str, Any]] = []
    page_token: str | None = None

    while True:
        params: dict[str, Any] = {"pageSize": 100}
        if page_token:
            params["pageToken"] = page_token

        data = request_json(
            session,
            f"{BASE_URL}/applications/{package_name}/oneTimeProducts",
            params,
        )
        products.extend(data.get("oneTimeProducts", []))

        page_token = data.get("nextPageToken")
        if not page_token:
            return products


def list_inapp_products(session: Any, package_name: str) -> list[dict[str, Any]]:
    products: list[dict[str, Any]] = []
    token: str | None = None

    while True:
        params: dict[str, Any] = {"maxResults": 100}
        if token:
            params["token"] = token

        data = request_json(
            session,
            f"{BASE_URL}/applications/{package_name}/inappproducts",
            params,
        )
        products.extend(data.get("inappproduct", []))

        token_pagination = data.get("tokenPagination", {})
        token = token_pagination.get("nextPageToken")
        if not token:
            return products


def product_id(product: dict[str, Any]) -> str:
    return product.get("productId") or product.get("sku") or ""


def filter_products(
    products: list[dict[str, Any]],
    include_all_products: bool,
    product_id_contains: str,
) -> list[dict[str, Any]]:
    if include_all_products:
        return products

    return [
        product
        for product in products
        if product_id_contains.lower() in product_id(product).lower()
    ]


def default_output_path() -> Path:
    timestamp = datetime.now(timezone.utc).strftime("%Y%m%d-%H%M%S")
    return Path("build/reports/play-products") / f"catalog-{timestamp}.json"


def write_output(path: Path, payload: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        json.dumps(payload, ensure_ascii=False, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )


def print_summary(payload: dict[str, Any]) -> None:
    print(f"Package: {payload['packageName']}")
    print(f"Output: {payload['outputPath']}")

    for api_name, section in payload["apis"].items():
        if "error" in section:
            print(f"{api_name}: ERROR")
            print(f"  {section['error']}")
            continue

        products = section["products"]
        print(f"{api_name}: {len(products)} product(s)")
        for product in products:
            print(f"  - {product_id(product)}")


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

    session = create_authorized_session(str(credentials_path))
    output_path = Path(args.output) if args.output else default_output_path()

    payload: dict[str, Any] = {
        "exportedAt": datetime.now(timezone.utc).isoformat(),
        "packageName": args.package_name,
        "filter": {
            "includeAllProducts": args.include_all_products,
            "productIdContains": None if args.include_all_products else args.product_id_contains,
        },
        "outputPath": str(output_path),
        "apis": {},
    }

    if args.api in ("both", "onetimeproducts"):
        try:
            products = list_onetime_products(session, args.package_name)
            payload["apis"]["onetimeproducts"] = {
                "products": filter_products(
                    products,
                    args.include_all_products,
                    args.product_id_contains,
                )
            }
        except Exception as error:
            payload["apis"]["onetimeproducts"] = {"error": str(error)}

    if args.api in ("both", "inappproducts"):
        try:
            products = list_inapp_products(session, args.package_name)
            payload["apis"]["inappproducts"] = {
                "products": filter_products(
                    products,
                    args.include_all_products,
                    args.product_id_contains,
                )
            }
        except Exception as error:
            payload["apis"]["inappproducts"] = {"error": str(error)}

    write_output(output_path, payload)
    print_summary(payload)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
