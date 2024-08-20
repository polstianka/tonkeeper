curl -o openapi.yml 'https://raw.githubusercontent.com/tonkeeper/opentonapi/master/api/openapi.yml'
curl -o battery-api.yml 'https://raw.githubusercontent.com/tonkeeper/custodial-battery/master/api/battery-api.yml'

openapi-generator generate -i openapi.yml -g kotlin --additional-properties packageName=io.tonapi
openapi-generator generate -i battery-api.yml -g kotlin --additional-properties packageName=io.batteryapi

# Rename the file
mv src/main/kotlin/io/batteryapi/apis/DefaultApi.kt src/main/kotlin/io/batteryapi/apis/BatteryApi.kt
# Replace the class name inside the file
sed -i '' 's/class DefaultApi/class BatteryApi/' src/main/kotlin/io/batteryapi/apis/BatteryApi.kt

rm -rf openapi.yml
rm -rf battery-api.yml
rm -rf settings.gradle
rm -rf docs
rm -rf gradle
rm -rf .openapi-generator
rm -rf README.md
rm -rf build.gradle
rm -rf .openapi-generator-ignore
rm -rf gradlew
rm -rf gradlew.bat
rm -rf src/test
