curl -s "https://repo1.maven.org/maven2/com/microsoft/playwright/playwright/maven-metadata.xml" \
      | grep -Po '<version>\K[^<]+' \
      | sed 's/\([0-9]*\.[0-9]*\)\.[0-9]*/\1/' \
      | sort -rV \
      | uniq \
      | head -n 10 \
      | while read -r version; do
          echo "Building $version"
          docker build -t ghcr.io/orange-buffalo/testcontainers-playwright:$version --build-arg PLAYWRIGHT_VERSION=$version src/main/docker
          docker push ghcr.io/orange-buffalo/testcontainers-playwright:$version
          echo "$version built"
          echo ""
        done
