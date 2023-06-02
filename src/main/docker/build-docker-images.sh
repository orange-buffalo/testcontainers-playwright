#!/bin/bash

# Fetch latest versions
curl -s "https://repo1.maven.org/maven2/com/microsoft/playwright/playwright/maven-metadata.xml" \
      | grep -Po '<version>\K[^<]+' \
      | sed 's/\([0-9]*\.[0-9]*\)\.[0-9]*/\1/' \
      | sort -rV \
      | uniq \
      | head -n 10 \
      | while read -r VERSION; do
          # Default OS
          BASE_OS="focal"

          # Use jammy for versions 1.33 and later
          if printf '%s\n' "1.33" "$VERSION" | sort -V -C; then
             BASE_OS="jammy"
          fi

          echo "Building $VERSION"
          docker build \
            --build-arg PLAYWRIGHT_VERSION=$VERSION \
            --build-arg BASE_OS=$BASE_OS \
            -t ghcr.io/orange-buffalo/testcontainers-playwright:$VERSION \
            src/main/docker
          docker push ghcr.io/orange-buffalo/testcontainers-playwright:$VERSION
          echo "$VERSION built"
          echo ""
        done
