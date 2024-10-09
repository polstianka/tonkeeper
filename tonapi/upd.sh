openapi-generator generate -i tonapi.yml -g kotlin --additional-properties=packageName=io.tonapi,serializationLibrary=kotlinx_serialization
openapi-generator generate -i battery-api.yml -g kotlin --additional-properties=packageName=io.batteryapi,serializationLibrary=kotlinx_serialization

# Rename the file
mv src/main/kotlin/io/batteryapi/apis/DefaultApi.kt src/main/kotlin/io/batteryapi/apis/BatteryApi.kt
# Replace the class name inside the file
sed -i '' 's/class DefaultApi/class BatteryApi/' src/main/kotlin/io/batteryapi/apis/BatteryApi.kt

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
