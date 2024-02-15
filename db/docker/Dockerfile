FROM eclipse-temurin:17-jre-jammy

ADD db-0.0.1.tar /root
WORKDIR /root/db-0.0.1

EXPOSE 4545

VOLUME /log

CMD ["./bin/db", "server", "--server-host", "0.0.0.0"]
