#!/usr/bin/env bash
# Sets versionName/versionCode in app/build.gradle.kts, creates the fastlane
# changelog files, then commits and tags the release locally. Pushing is left
# to the caller after review — see README "Releasing".
#
# Usage: ./scripts/bump-version.sh MAJOR.MINOR.PATCH
#
# versionCode = MAJOR * 10000 + MINOR * 100 + PATCH, so it always increases
# with the version and never has to be tracked separately. Both values stay
# literals in build.gradle.kts because F-Droid's update checker reads them
# from there.
set -euo pipefail

cd "$(dirname "$0")/.."

gradle_file=app/build.gradle.kts
changelog_dirs=(fastlane/metadata/android/en-US/changelogs fastlane/metadata/android/de-DE/changelogs)

die() {
    echo "error: $*" >&2
    exit 1
}

[[ $# -eq 1 ]] || die "usage: $0 MAJOR.MINOR.PATCH (e.g. 1.2.2)"
version=$1

# Minor and patch must stay below 100 or they'd overflow into the next
# component of versionCode.
[[ $version =~ ^(0|[1-9][0-9]*)\.(0|[1-9][0-9]?)\.(0|[1-9][0-9]?)$ ]] ||
    die "'$version' is not MAJOR.MINOR.PATCH with minor and patch below 100"
version_code=$((BASH_REMATCH[1] * 10000 + BASH_REMATCH[2] * 100 + BASH_REMATCH[3]))
tag="v$version"

current_name=$(sed -n 's/^ *versionName = "\(.*\)"$/\1/p' "$gradle_file")
current_code=$(sed -n 's/^ *versionCode = \([0-9]*\)$/\1/p' "$gradle_file")
[[ -n $current_name && -n $current_code ]] || die "could not read versionName/versionCode from $gradle_file"

# Validate everything before changing anything, so a refused run leaves the
# working tree untouched.
[[ $version != "$current_name" ]] &&
    [[ $(printf '%s\n%s\n' "$current_name" "$version" | sort -V | tail -n1) == "$version" ]] ||
    die "$version is not higher than the current version $current_name"
((version_code > current_code)) ||
    die "versionCode $version_code is not higher than the current versionCode $current_code"
[[ -z $(git status --porcelain) ]] || die "working tree is not clean; commit or stash first"
! git rev-parse -q --verify "refs/tags/$tag" >/dev/null || die "tag $tag already exists"
for dir in "${changelog_dirs[@]}"; do
    [[ ! -e $dir/$version_code.txt ]] || die "$dir/$version_code.txt already exists"
done

sed -i \
    -e "s/^\( *versionCode = \)[0-9]*$/\1$version_code/" \
    -e "s/^\( *versionName = \)\".*\"$/\1\"$version\"/" \
    "$gradle_file"

for dir in "${changelog_dirs[@]}"; do
    echo "TODO: describe the changes in $version" >"$dir/$version_code.txt"
done

git add "$gradle_file" "${changelog_dirs[@]/%//$version_code.txt}"
git commit -q -m "chore: release $version"
git tag -a "$tag" -m "Release $version"

echo "Version set to $version (versionCode $version_code), committed and tagged $tag."
echo "Next: fill in fastlane/metadata/android/*/changelogs/$version_code.txt,"
echo "amend the commit and move the tag (git commit --amend && git tag -f -a $tag -m \"Release $version\"),"
echo "then: git push && git push origin $tag"
