#!/usr/bin/env bash
# Run by release.yml before building: checks that the release tag matches the
# version in app/build.gradle.kts, that versionCode increased since the
# previous v* tag and that the changelogs for the new versionCode exist.
#
# Usage: ./scripts/check-release-version.sh <tag>   (e.g. v1.2.2)
# Needs the full tag history (actions/checkout with fetch-depth: 0).
set -euo pipefail

cd "$(dirname "$0")/.."

gradle_file=app/build.gradle.kts
changelog_dirs=(fastlane/metadata/android/en-US/changelogs fastlane/metadata/android/de-DE/changelogs)

[[ $# -eq 1 ]] || {
    echo "usage: $0 <tag>" >&2
    exit 1
}
tag=$1
failed=0

fail() {
    echo "::error::$*"
    failed=1
}

read_code() {
    sed -n 's/^ *versionCode = \([0-9]*\)$/\1/p'
}

version_name=$(sed -n 's/^ *versionName = "\(.*\)"$/\1/p' "$gradle_file")
version_code=$(read_code <"$gradle_file")

[[ $tag == "v$version_name" ]] ||
    fail "tag $tag does not match versionName \"$version_name\" in $gradle_file (expected tag v$version_name)"

if previous_tag=$(git describe --tags --abbrev=0 --match 'v*' "$tag^" 2>/dev/null); then
    previous_code=$(git show "$previous_tag:$gradle_file" | read_code)
    ((version_code > previous_code)) ||
        fail "versionCode $version_code is not higher than $previous_code in the previous tag $previous_tag"
else
    echo "No previous v* tag found; skipping the versionCode comparison."
fi

for dir in "${changelog_dirs[@]}"; do
    [[ -f $dir/$version_code.txt ]] || fail "changelog $dir/$version_code.txt is missing"
done

((failed == 0)) || exit 1
echo "Release $tag: versionName $version_name, versionCode $version_code - OK"
