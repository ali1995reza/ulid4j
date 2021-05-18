package ulid4j.api;

public interface ULIDGenerator {

    ULID generate();

    ULID from(String from);

}
