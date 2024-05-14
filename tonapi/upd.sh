wget https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/7.5.0/openapi-generator-cli-7.5.0.jar -O openapi-generator-cli.jar
curl -o openapi.yml 'https://raw.githubusercontent.com/tonkeeper/opentonapi/master/api/openapi.yml'

java -jar openapi-generator-cli.jar generate -i openapi.yml -g kotlin --additional-properties packageName=io.tonapi

rm -rf openapi.yml
rm -rf settings.gradle
rm -rf docs
rm -rf gradle
rm -rf .openapi-generator
rm -rf README.md
rm -rf build.gradle
rm -rf .openapi-generator-ignore
rm -rf gradlew
rm -rf gradlew.bat
rm openapi-generator-cli.jar
