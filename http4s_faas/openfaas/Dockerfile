FROM java:8-jre-alpine
# Use any image as your base image, or "scratch"
# Then set fprocess to the process you want to invoke per request - i.e. "cat" or "my_binary"

ENV SBT_VERSION 1.2.1

RUN apk add --no-cache bash curl openrc git && \
    curl -sL "https://github.com/sbt/sbt/releases/download/v${SBT_VERSION}/sbt-${SBT_VERSION}.tgz" | gunzip | tar -x -C /usr/local && \
    ln -s /usr/local/sbt/bin/sbt /usr/local/bin/sbt && \
    chmod 0755 /usr/local/bin/sbt && \
    apk add --no-cache --repository http://dl-cdn.alpinelinux.org/alpine/edge/main --repository  http://dl-cdn.alpinelinux.org/alpine/edge/community docker

RUN apk add --no-cache curl && \
    curl -sfL \
    https://github.com/openfaas-incubator/of-watchdog/releases/download/0.4.0/of-watchdog > /usr/bin/fwatchdog && \
    chmod +x /usr/bin/fwatchdog

RUN sbt sbtVersion

COPY ./app.jar /app.jar

ENV fprocess="java -jar /app.jar"
ENV mode=http
ENV upstream_url=http://127.0.0.1:5000
ENV read_timeout=20
RUN touch /tmp/.lock

HEALTHCHECK --interval=5s CMD [ -e /tmp/.lock ] || exit 1
EXPOSE 8080
CMD ["fwatchdog"]
