#!/usr/bin/env python3
"""
Audit Android signing-key exposure and optionally compare Play signing hashes.

This script is intentionally conservative: it never resets or rotates keys.
Google Play upload-key reset and app-signing-key upgrade are Play Console flows,
not public Android Publisher API operations.
"""

from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Any


ANDROID_PUBLISHER_SCOPE = "https://www.googleapis.com/auth/androidpublisher"
BASE_URL = "https://androidpublisher.googleapis.com/androidpublisher/v3"
DEFAULT_PACKAGE_NAME = "es.soutullo.blitter"
DEFAULT_KEYSTORE = "keystore.jks"
DEFAULT_REPORT = "build/reports/signing-security-audit.json"
SECRET_REFERENCE_REGEX = re.compile(
    r"(storePassword|keyPassword|storeFile|keyAlias|keystore\.jks|\.jks|"
    r"GOOGLE_APPLICATION_CREDENTIALS|client_email|private_key|refresh_token)",
    re.IGNORECASE,
)
SCAN_EXCLUDES = (
    ".git",
    ".gradle",
    ".idea",
    ".kotlin",
    ".venv-play-api",
    "app/build",
    "build",
)
SCAN_FILE_EXCLUDES = {
    ".gitignore",
    "scripts/audit_signing_security.py",
}
GITIGNORE_ENTRIES = (
    "*.jks",
    "*.keystore",
    "*service-account*.json",
    "*credentials*.json",
    ".secrets/",
    ".kotlin/",
)


@dataclass(frozen=True)
class CommandResult:
    returncode: int
    stdout: str
    stderr: str


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Audit Android signing key exposure and Play signing fingerprints."
    )
    parser.add_argument(
        "--keystore",
        default=DEFAULT_KEYSTORE,
        help=f"Keystore path to audit. Defaults to {DEFAULT_KEYSTORE}.",
    )
    parser.add_argument(
        "--keystore-password-env",
        default="BLITTER_KEYSTORE_PASSWORD",
        help="Environment variable containing the keystore password for local fingerprints.",
    )
    parser.add_argument(
        "--alias",
        help="Optional keystore alias to inspect. If omitted, keytool lists all aliases.",
    )
    parser.add_argument(
        "--credentials",
        default=os.environ.get("GOOGLE_APPLICATION_CREDENTIALS"),
        help="Optional service account JSON. Defaults to GOOGLE_APPLICATION_CREDENTIALS.",
    )
    parser.add_argument(
        "--package-name",
        default=DEFAULT_PACKAGE_NAME,
        help=f"Android package name. Defaults to {DEFAULT_PACKAGE_NAME}.",
    )
    parser.add_argument(
        "--play-version-code",
        type=int,
        help="Optional app bundle versionCode to inspect with generatedApks.list.",
    )
    parser.add_argument(
        "--discover-play-version-code",
        action="store_true",
        help="Read Play tracks through a temporary edit and inspect the highest versionCode found.",
    )
    parser.add_argument(
        "--fix-gitignore",
        action="store_true",
        help="Add safe signing/credential ignore patterns to .gitignore.",
    )
    parser.add_argument(
        "--output",
        default=DEFAULT_REPORT,
        help=f"Write JSON report here. Defaults to {DEFAULT_REPORT}.",
    )
    return parser.parse_args()


def run(command: list[str], cwd: Path) -> CommandResult:
    result = subprocess.run(
        command,
        cwd=cwd,
        check=False,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    return CommandResult(result.returncode, result.stdout.strip(), result.stderr.strip())


def git_lines(command: list[str], cwd: Path) -> list[str]:
    result = run(command, cwd)
    if result.returncode != 0:
        return []
    return [line for line in result.stdout.splitlines() if line.strip()]


def is_excluded(path: Path) -> bool:
    normalized = path.as_posix()
    return any(
        normalized == excluded or normalized.startswith(f"{excluded}/")
        for excluded in SCAN_EXCLUDES
    )


def scan_secret_references(root: Path) -> list[dict[str, Any]]:
    matches: list[dict[str, Any]] = []

    for path in sorted(root.rglob("*")):
        relative_path = path.relative_to(root)
        relative_path_text = relative_path.as_posix()
        if (
            is_excluded(relative_path)
            or relative_path_text in SCAN_FILE_EXCLUDES
            or not path.is_file()
        ):
            continue

        try:
            content = path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue

        for line_number, line in enumerate(content.splitlines(), start=1):
            if is_potential_secret_reference(line):
                matches.append(
                    {
                        "path": relative_path_text,
                        "line": line_number,
                        "text": redact_line(line.strip()),
                    }
                )

    return matches


def is_potential_secret_reference(line: str) -> bool:
    if not SECRET_REFERENCE_REGEX.search(line):
        return False

    benign_fragments = (
        "os.environ.get(",
        "Defaults to GOOGLE_APPLICATION_CREDENTIALS",
        "GOOGLE_APPLICATION_CREDENTIALS is not set",
        "export GOOGLE_APPLICATION_CREDENTIALS=/absolute/path/",
    )
    return not any(fragment in line for fragment in benign_fragments)


def redact_line(line: str) -> str:
    redacted = re.sub(
        r"(?i)(password|private_key|refresh_token)(\s*[:=]\s*)['\"]?[^,'\"]+",
        r"\1\2<redacted>",
        line,
    )
    return redacted[:300]


def audit_gitignore(root: Path, fix: bool) -> dict[str, Any]:
    gitignore_path = root / ".gitignore"
    content = gitignore_path.read_text(encoding="utf-8") if gitignore_path.exists() else ""
    existing_lines = {line.strip() for line in content.splitlines()}
    missing = [entry for entry in GITIGNORE_ENTRIES if entry not in existing_lines]

    if fix and missing:
        suffix = "" if content.endswith("\n") or not content else "\n"
        gitignore_path.write_text(
            content + suffix + "\n".join(missing) + "\n",
            encoding="utf-8",
        )

    return {"missingEntries": missing, "fixed": bool(fix and missing)}


def audit_keystore(root: Path, keystore: Path, password_env: str, alias: str | None) -> dict[str, Any]:
    absolute_keystore = keystore if keystore.is_absolute() else root / keystore
    relative_keystore = absolute_keystore.relative_to(root) if absolute_keystore.is_relative_to(root) else absolute_keystore
    tracked = bool(git_lines(["git", "ls-files", "--", str(relative_keystore)], root))
    history = git_lines(
        [
            "git",
            "log",
            "--all",
            "--format=%h %ad %s",
            "--date=short",
            "--",
            str(relative_keystore),
        ],
        root,
    )

    report: dict[str, Any] = {
        "path": str(relative_keystore),
        "exists": absolute_keystore.exists(),
        "tracked": tracked,
        "history": history,
        "fingerprints": None,
    }

    if not absolute_keystore.exists():
        return report

    password = os.environ.get(password_env)
    if not password:
        report["fingerprintsError"] = (
            f"Set {password_env} to let keytool read SHA-1/SHA-256 fingerprints."
        )
        return report

    command = [
        "keytool",
        "-list",
        "-v",
        "-keystore",
        str(absolute_keystore),
        "-storepass",
        password,
    ]
    if alias:
        command.extend(["-alias", alias])

    result = run(command, root)
    if result.returncode != 0:
        report["fingerprintsError"] = result.stderr or result.stdout
        return report

    report["fingerprints"] = parse_keytool_fingerprints(result.stdout)
    return report


def parse_keytool_fingerprints(output: str) -> list[dict[str, str]]:
    fingerprints: list[dict[str, str]] = []
    current: dict[str, str] = {}

    for raw_line in output.splitlines():
        line = raw_line.strip()
        if line.startswith("Alias name:"):
            if current:
                fingerprints.append(current)
            current = {"alias": line.split(":", maxsplit=1)[1].strip()}
        elif line.startswith("SHA1:") and current:
            current["sha1"] = normalize_hash(line.split(":", maxsplit=1)[1])
        elif line.startswith("SHA256:") and current:
            current["sha256"] = normalize_hash(line.split(":", maxsplit=1)[1])

    if current:
        fingerprints.append(current)

    return fingerprints


def normalize_hash(value: str) -> str:
    return value.strip().replace(":", "").upper()


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
            f"{response.text[:1000]}"
        )
    return response.json() if response.text else {}


def create_edit(session: Any, package_name: str) -> str:
    edit = request_json(
        session,
        "POST",
        f"{BASE_URL}/applications/{package_name}/edits",
        json={},
    )
    return edit["id"]


def discover_highest_play_version_code(session: Any, package_name: str) -> int | None:
    edit_id = create_edit(session, package_name)
    try:
        tracks = request_json(
            session,
            "GET",
            f"{BASE_URL}/applications/{package_name}/edits/{edit_id}/tracks",
        ).get("tracks", [])
    finally:
        session.delete(f"{BASE_URL}/applications/{package_name}/edits/{edit_id}")

    version_codes: list[int] = []
    for track in tracks:
        for release in track.get("releases", []):
            for version_code in release.get("versionCodes", []):
                version_codes.append(int(version_code))

    return max(version_codes) if version_codes else None


def inspect_play_artifacts(session: Any, package_name: str) -> dict[str, Any]:
    edit_id = create_edit(session, package_name)

    try:
        tracks = request_json(
            session,
            "GET",
            f"{BASE_URL}/applications/{package_name}/edits/{edit_id}/tracks",
        ).get("tracks", [])
        apks = request_json(
            session,
            "GET",
            f"{BASE_URL}/applications/{package_name}/edits/{edit_id}/apks",
        ).get("apks", [])
        bundles = request_json(
            session,
            "GET",
            f"{BASE_URL}/applications/{package_name}/edits/{edit_id}/bundles",
        ).get("bundles", [])
    finally:
        session.delete(f"{BASE_URL}/applications/{package_name}/edits/{edit_id}")

    return {
        "tracks": summarize_tracks(tracks),
        "apkVersionCodes": sorted(apk.get("versionCode") for apk in apks),
        "bundleVersionCodes": sorted(bundle.get("versionCode") for bundle in bundles),
        "apks": apks,
        "bundles": bundles,
    }


def summarize_tracks(tracks: list[dict[str, Any]]) -> list[dict[str, Any]]:
    summary: list[dict[str, Any]] = []
    for track in tracks:
        releases = [
            {
                "name": release.get("name"),
                "status": release.get("status"),
                "versionCodes": release.get("versionCodes", []),
            }
            for release in track.get("releases", [])
        ]
        summary.append({"track": track.get("track"), "releases": releases})
    return summary


def audit_play_signing(
    credentials_path: str | None,
    package_name: str,
    version_code: int | None,
    discover_version_code: bool,
) -> dict[str, Any]:
    report: dict[str, Any] = {
        "packageName": package_name,
        "versionCode": version_code,
        "generatedApksCertificateSha256Hashes": [],
    }

    if not credentials_path:
        report["skipped"] = "GOOGLE_APPLICATION_CREDENTIALS/--credentials not provided."
        return report

    credentials = Path(credentials_path).expanduser()
    if not credentials.exists():
        report["error"] = f"Credentials file does not exist: {credentials}"
        return report

    try:
        session = create_authorized_session(str(credentials))
        report["artifacts"] = inspect_play_artifacts(session, package_name)

        if discover_version_code and version_code is None:
            version_code = discover_highest_play_version_code(session, package_name)
            report["versionCode"] = version_code

        if version_code is None:
            report["skipped"] = "Pass --play-version-code or --discover-play-version-code."
            return report

        generated = request_json(
            session,
            "GET",
            f"{BASE_URL}/applications/{package_name}/generatedApks/{version_code}",
        )
        report["generatedApksCertificateSha256Hashes"] = sorted(
            {
                normalize_hash(item["certificateSha256Hash"])
                for item in generated.get("generatedApks", [])
                if item.get("certificateSha256Hash")
            }
        )
    except Exception as error:
        error_text = str(error)
        report["error"] = error_text
        artifacts = report.get("artifacts", {})
        if "could not be found" in error_text and version_code in artifacts.get("apkVersionCodes", []):
            report["artifactKind"] = "apk"
            report["generatedApksUnavailableReason"] = (
                "The selected versionCode is a legacy APK. generatedApks.list only works "
                "for APKs generated from an App Bundle."
            )
            report.pop("error", None)

    return report


def classify_risk(report: dict[str, Any]) -> list[str]:
    risks: list[str] = []
    keystore = report["keystore"]

    if keystore["tracked"]:
        risks.append("keystore_tracked_in_git")
    if keystore["history"]:
        risks.append("keystore_present_in_git_history")
    if report["secretReferences"]:
        risks.append("signing_or_credential_references_found")

    local_hashes = {
        fingerprint["sha256"]
        for fingerprint in keystore.get("fingerprints") or []
        if fingerprint.get("sha256")
    }
    play_hashes = set(report["playSigning"].get("generatedApksCertificateSha256Hashes") or [])
    if local_hashes and play_hashes:
        report["localMatchesPlaySigningKey"] = bool(local_hashes & play_hashes)
        if local_hashes & play_hashes:
            risks.append("local_keystore_matches_play_delivered_signing_key")
    else:
        report["localMatchesPlaySigningKey"] = None

    return risks


def build_recommendations(report: dict[str, Any]) -> list[str]:
    recommendations = []
    keystore = report["keystore"]

    if keystore["tracked"]:
        recommendations.append("Remove keystore.jks from the git index after rotating/replacing the key.")
    if keystore["history"]:
        recommendations.append("Treat this keystore as exposed if the repository was ever public.")
    if report.get("localMatchesPlaySigningKey") is True:
        recommendations.append(
            "The local keystore SHA-256 matches a Play-delivered signing key; review Play App Signing key upgrade options."
        )
    else:
        recommendations.append(
            "If this is only an upload key, generate a new upload key and request reset in Play Console."
        )
    if report["gitignore"]["missingEntries"]:
        recommendations.append("Run with --fix-gitignore to add missing ignore patterns.")

    recommendations.append("Do not commit service account JSON files or keystore passwords.")
    return recommendations


def print_summary(report: dict[str, Any]) -> None:
    keystore = report["keystore"]
    print(f"Keystore: {keystore['path']}")
    print(f"  exists: {keystore['exists']}")
    print(f"  tracked: {keystore['tracked']}")
    print(f"  history entries: {len(keystore['history'])}")

    fingerprints = keystore.get("fingerprints")
    if fingerprints:
        print("  local fingerprints:")
        for fingerprint in fingerprints:
            alias = fingerprint.get("alias", "<unknown>")
            sha256 = fingerprint.get("sha256", "<missing>")
            print(f"    - {alias}: SHA-256 {sha256}")
    elif keystore.get("fingerprintsError"):
        print(f"  local fingerprints: {keystore['fingerprintsError']}")

    play_hashes = report["playSigning"].get("generatedApksCertificateSha256Hashes") or []
    if play_hashes:
        print("Play generated APK signing SHA-256:")
        for hash_value in play_hashes:
            print(f"  - {hash_value}")
    elif report["playSigning"].get("skipped"):
        print(f"Play signing: skipped ({report['playSigning']['skipped']})")
    elif report["playSigning"].get("generatedApksUnavailableReason"):
        print(f"Play signing: {report['playSigning']['generatedApksUnavailableReason']}")
    elif report["playSigning"].get("error"):
        print(f"Play signing: error ({report['playSigning']['error']})")

    artifacts = report["playSigning"].get("artifacts") or {}
    if artifacts:
        print(f"Play APK versionCodes: {artifacts.get('apkVersionCodes', [])}")
        print(f"Play AAB versionCodes: {artifacts.get('bundleVersionCodes', [])}")

    print("Risks:")
    for risk in report["risks"] or ["none_detected"]:
        print(f"  - {risk}")

    print("Recommendations:")
    for recommendation in report["recommendations"]:
        print(f"  - {recommendation}")

    print(f"Report: {report['outputPath']}")


def main() -> int:
    args = parse_args()
    root = Path.cwd()

    report: dict[str, Any] = {
        "keystore": audit_keystore(
            root,
            Path(args.keystore),
            args.keystore_password_env,
            args.alias,
        ),
        "gitignore": audit_gitignore(root, args.fix_gitignore),
        "secretReferences": scan_secret_references(root),
        "playSigning": audit_play_signing(
            args.credentials,
            args.package_name,
            args.play_version_code,
            args.discover_play_version_code,
        ),
        "outputPath": args.output,
    }
    report["risks"] = classify_risk(report)
    report["recommendations"] = build_recommendations(report)

    output_path = Path(args.output)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(
        json.dumps(report, ensure_ascii=False, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )
    print_summary(report)

    return 1 if report["risks"] else 0


if __name__ == "__main__":
    raise SystemExit(main())
