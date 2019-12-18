#!/bin/bash

cd server
# Start server
./gradlew run & sleep 5 && cd .. && cd client && ./gradlew -PmainClass=KtorKt run ; ./gradlew -PmainClass=OkhttpKt run ; cd .. && cd server && ./gradlew --stop
lsof -ti:5003 | xargs kill
