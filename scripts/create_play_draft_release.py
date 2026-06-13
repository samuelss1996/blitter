#!/usr/bin/env python3
"""
Build, sign, verify and upload a draft APK release to Google Play.

The script intentionally requires an explicit --commit flag before it creates
the draft release in Play Console. Without --commit it only builds and signs.
Keystore passwords can be provided through environment variables or typed at a
local prompt; they are never written to disk.
"""

from __future__ import annotations

import argparse
import getpass
import json
import os
import shutil
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


ANDROID_PUBLISHER_SCOPE = "https://www.googleapis.com/auth/androidpublisher"
BASE_URL = "https://androidpublisher.googleapis.com/androidpublisher/v3"
UPLOAD_BASE_URL = "https://androidpublisher.googleapis.com/upload/androidpublisher/v3"
DEFAULT_PACKAGE_NAME = "es.soutullo.blitter"
DEFAULT_KEYSTORE = "keystore.jks"
DEFAULT_KEY_ALIAS = "key0"
DEFAULT_VERSION_NAME = "2.3.0"
DEFAULT_RELEASE_NAME = "2.3.0"
EXPECTED_CERT_SHA256 = "B66A53B68068E46D531A338EB3EBB12B82D3BE9518F026BA177180C14CD099BD"
BUILD_TOOLS_CANDIDATES = (
    Path(os.environ.get("ANDROID_HOME", "")) / "build-tools",
    Path(os.environ.get("ANDROID_SDK_ROOT", "")) / "build-tools",
    Path("/home/samuel/Android/Sdk/build-tools"),
)
DEFAULT_RELEASE_NOTES = [
    {
        "language": "en-GB",
        "text": (
            "Compatibility update for recent Android versions.\n"
            "Updated ads and billing libraries.\n"
            "Fixed visual issues around system bars and selection mode."
        ),
    },
    {
        "language": "es-419",
        "text": (
            "Actualización de compatibilidad con versiones recientes de Android.\n"
            "Bibliotecas de anuncios y pagos actualizadas.\n"
            "Correcciones visuales en barras del sistema y modo de selección."
        ),
    },
    {
        "language": "es-ES",
        "text": (
            "Actualización de compatibilidad con versiones recientes de Android.\n"
            "Bibliotecas de anuncios y pagos actualizadas.\n"
            "Correcciones visuales en barras del sistema y modo de selección."
        ),
    },
    {
        "language": "es-US",
        "text": (
            "Actualización de compatibilidad con versiones recientes de Android.\n"
            "Bibliotecas de anuncios y pagos actualizadas.\n"
            "Correcciones visuales en barras del sistema y modo de selección."
        ),
    },
    {
        "language": "gl-ES",
        "text": (
            "Actualización de compatibilidade con versións recentes de Android.\n"
            "Bibliotecas de anuncios e pagamentos actualizadas.\n"
            "Correccións visuais nas barras do sistema e no modo de selección."
        ),
    },
    {
        "language": "pt-BR",
        "text": (
            "Atualização de compatibilidade com versões recentes do Android.\n"
            "Bibliotecas de anúncios e pagamentos atualizadas.\n"
            "Correções visuais nas barras do sistema e no modo de seleção."
        ),
    },
    {
        "language": "pt-PT",
        "text": (
            "Atualização de compatibilidade com versões recentes do Android.\n"
            "Bibliotecas de anúncios e pagamentos atualizadas.\n"
            "Correções visuais nas barras do sistema e no modo de seleção."
        ),
    },
]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Create a signed APK and optional Google Play draft release."
    )
    parser.add_argument(
        "--package-name",
        default=DEFAULT_PACKAGE_NAME,
        help=f"Android package name. Defaults to {DEFAULT_PACKAGE_NAME}.",
    )
    parser.add_argument(
        "--credentials",
        default=os.environ.get("GOOGLE_APPLICATION_CREDENTIALS"),
        help="Service account JSON. Defaults to GOOGLE_APPLICATION_CREDENTIALS.",
    )
    parser.add_argument(
        "--keystore",
        default=DEFAULT_KEYSTORE,
        help=f"Release keystore path. Defaults to {DEFAULT_KEYSTORE}.",
    )
    parser.add_argument(
        "--key-alias",
        default=DEFAULT_KEY_ALIAS,
        help=f"Signing key alias. Defaults to {DEFAULT_KEY_ALIAS}.",
    )
    parser.add_argument(
        "--keystore-password-env",
        default="BLITTER_KEYSTORE_PASSWORD",
        help="Environment variable for the keystore password.",
    )
    parser.add_argument(
        "--key-password-env",
        default="BLITTER_KEY_PASSWORD",
        help="Environment variable for the key password. Defaults to keystore password.",
    )
    parser.add_argument(
        "--track",
        default="production",
        help="Google Play track to update. Defaults to production.",
    )
    parser.add_argument(
        "--release-name",
        default=DEFAULT_RELEASE_NAME,
        help=f"Release name shown in Play Console. Defaults to {DEFAULT_RELEASE_NAME}.",
    )
    parser.add_argument(
        "--status",
        choices=("draft", "completed", "inProgress"),
        default="draft",
        help="Release status. Defaults to draft.",
    )
    parser.add_argument(
        "--user-fraction",
        type=float,
        help="Required by Google Play for inProgress staged rollouts.",
    )
    parser.add_argument(
        "--expected-cert-sha256",
        default=EXPECTED_CERT_SHA256,
        help="Expected signer certificate SHA-256 without colons.",
    )
    parser.add_argument(
        "--release-notes",
        help="Optional release-notes JSON file. Defaults to the built-in 2.3.0 notes.",
    )
    parser.add_argument(
        "--unsigned-apk",
        default="app/build/outputs/apk/release/app-release-unsigned.apk",
        help="Unsigned APK generated by assembleRelease.",
    )
    parser.add_argument(
        "--aligned-apk",
        default="build/outputs/play-release/blitter-2.3.0-aligned.apk",
        help="Temporary zipaligned APK path.",
    )
    parser.add_argument(
        "--signed-apk",
        default="build/outputs/play-release/blitter-2.3.0-release.apk",
        help="Signed APK output path.",
    )
    parser.add_argument(
        "--report",
        default="build/reports/play-release/draft-release-2.3.0.json",
        help="Write a JSON report here.",
    )
    parser.add_argument(
        "--commit",
        action="store_true",
        help="Actually upload and commit the draft release to Play Console.",
    )
    return parser.parse_args()


def run(command: list[str], cwd: Path, env: dict[str, str] | None = None) -> str:
    result = subprocess.run(
        command,
        cwd=cwd,
        env=env,
        check=False,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    if result.returncode != 0:
        raise RuntimeError(
            f"Command failed ({result.returncode}): {' '.join(command)}\n{result.stdout}"
        )
    return result.stdout


def latest_build_tool(tool_name: str) -> Path:
    for build_tools_root in BUILD_TOOLS_CANDIDATES:
        if not build_tools_root.exists():
            continue

        versions = sorted(build_tools_root.iterdir(), key=lambda path: path.name, reverse=True)
        for version_dir in versions:
            candidate = version_dir / tool_name
            if candidate.exists():
                return candidate

    raise FileNotFoundError(f"Could not find Android build-tool: {tool_name}")


def read_password(env_name: str, prompt: str) -> str:
    value = os.environ.get(env_name)
    if value:
        return value

    if not sys.stdin.isatty():
        raise RuntimeError(f"{env_name} is not set and no interactive terminal is available.")

    return getpass.getpass(prompt)


def build_release(root: Path) -> None:
    run(["./gradlew", ":app:assembleRelease"], root)


def sign_apk(root: Path, args: argparse.Namespace) -> None:
    unsigned_apk = root / args.unsigned_apk
    aligned_apk = root / args.aligned_apk
    signed_apk = root / args.signed_apk
    keystore = root / args.keystore if not Path(args.keystore).is_absolute() else Path(args.keystore)

    if not unsigned_apk.exists():
        raise FileNotFoundError(f"Unsigned APK not found: {unsigned_apk}")
    if not keystore.exists():
        raise FileNotFoundError(f"Keystore not found: {keystore}")

    aligned_apk.parent.mkdir(parents=True, exist_ok=True)
    signed_apk.parent.mkdir(parents=True, exist_ok=True)
    aligned_apk.unlink(missing_ok=True)
    signed_apk.unlink(missing_ok=True)

    zipalign = latest_build_tool("zipalign")
    apksigner = latest_build_tool("apksigner")
    store_password = read_password(args.keystore_password_env, "Keystore password: ")
    key_password = os.environ.get(args.key_password_env, store_password)

    run([str(zipalign), "-p", "-f", "4", str(unsigned_apk), str(aligned_apk)], root)
    sign_env = os.environ.copy()
    sign_env["BLITTER_STORE_PASS"] = store_password
    sign_env["BLITTER_KEY_PASS"] = key_password
    run(
        [
            str(apksigner),
            "sign",
            "--ks",
            str(keystore),
            "--ks-key-alias",
            args.key_alias,
            "--ks-pass",
            "env:BLITTER_STORE_PASS",
            "--key-pass",
            "env:BLITTER_KEY_PASS",
            "--out",
            str(signed_apk),
            str(aligned_apk),
        ],
        root,
        env=sign_env,
    )


def verify_signed_apk(root: Path, args: argparse.Namespace) -> dict[str, Any]:
    apksigner = latest_build_tool("apksigner")
    output = run(
        [str(apksigner), "verify", "--print-certs", str(root / args.signed_apk)],
        root,
    )
    expected = normalize_hash(args.expected_cert_sha256)
    if expected not in normalize_hash(output):
        raise RuntimeError(
            f"Signed APK certificate does not match expected SHA-256 {expected}.\n{output}"
        )

    return {
        "signedApk": args.signed_apk,
        "expectedCertSha256": expected,
        "verification": output,
    }


def normalize_hash(value: str) -> str:
    return value.replace(":", "").replace(" ", "").replace("\n", "").upper()


def load_release_notes(path: str | None) -> list[dict[str, str]]:
    if not path:
        return DEFAULT_RELEASE_NOTES

    payload = json.loads(Path(path).read_text(encoding="utf-8"))
    if isinstance(payload, dict):
        return payload["releaseNotes"]
    return payload


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


def request_json(session: Any, method: str, url: str, **kwargs: Any) -> dict[str, Any]:
    response = session.request(method, url, **kwargs)
    if response.status_code >= 400:
        raise RuntimeError(
            f"{method} {response.url} failed with HTTP {response.status_code}: "
            f"{response.text[:2_000]}"
        )
    return response.json() if response.text else {}


def upload_draft_release(root: Path, args: argparse.Namespace) -> dict[str, Any]:
    if not args.credentials:
        raise RuntimeError("GOOGLE_APPLICATION_CREDENTIALS is not set and --credentials was not provided.")

    credentials = Path(args.credentials).expanduser()
    if not credentials.exists():
        raise FileNotFoundError(f"Credentials file does not exist: {credentials}")

    signed_apk = root / args.signed_apk
    session = create_authorized_session(str(credentials))
    edit = request_json(
        session,
        "POST",
        f"{BASE_URL}/applications/{args.package_name}/edits",
        json={},
    )
    edit_id = edit["id"]

    try:
        with signed_apk.open("rb") as apk_file:
            upload_response = request_json(
                session,
                "POST",
                f"{UPLOAD_BASE_URL}/applications/{args.package_name}/edits/{edit_id}/apks",
                params={"uploadType": "media"},
                data=apk_file,
                headers={"Content-Type": "application/vnd.android.package-archive"},
            )

        version_code = str(upload_response["versionCode"])
        release: dict[str, Any] = {
            "name": args.release_name,
            "versionCodes": [version_code],
            "status": args.status,
            "releaseNotes": load_release_notes(args.release_notes),
        }
        if args.status == "inProgress":
            if args.user_fraction is None:
                raise RuntimeError("--user-fraction is required for inProgress releases.")
            release["userFraction"] = args.user_fraction

        track_response = request_json(
            session,
            "PUT",
            f"{BASE_URL}/applications/{args.package_name}/edits/{edit_id}/tracks/{args.track}",
            json={"track": args.track, "releases": [release]},
        )
        commit_response = request_json(
            session,
            "POST",
            f"{BASE_URL}/applications/{args.package_name}/edits/{edit_id}:commit",
        )

        return {
            "editId": edit_id,
            "upload": upload_response,
            "track": track_response,
            "commit": commit_response,
        }
    except Exception:
        session.delete(f"{BASE_URL}/applications/{args.package_name}/edits/{edit_id}")
        raise


def write_report(path: Path, report: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        json.dumps(report, ensure_ascii=False, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )


def print_summary(report: dict[str, Any]) -> None:
    print(f"Signed APK: {report['verification']['signedApk']}")
    print(f"Certificate SHA-256: {report['verification']['expectedCertSha256']}")

    play = report.get("play")
    if play:
        print(f"Uploaded versionCode: {play['upload']['versionCode']}")
        print(f"Track: {report['track']}")
        print(f"Status: {report['status']}")
        print("Play draft release committed.")
    else:
        print("Play upload skipped. Re-run with --commit to create the draft release.")

    print(f"Report: {report['reportPath']}")


def main() -> int:
    args = parse_args()
    root = Path.cwd()

    build_release(root)
    sign_apk(root, args)
    verification = verify_signed_apk(root, args)

    report: dict[str, Any] = {
        "createdAt": datetime.now(timezone.utc).isoformat(),
        "packageName": args.package_name,
        "track": args.track,
        "status": args.status,
        "releaseName": args.release_name,
        "verification": verification,
        "reportPath": args.report,
    }

    if args.commit:
        report["play"] = upload_draft_release(root, args)

    write_report(Path(args.report), report)
    print_summary(report)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
