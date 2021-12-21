FROM gradle:7.3.2-jdk11
ADD --chown=gradle . /code
WORKDIR /code
CMD ["gradle", "--stacktrace", "run"]
