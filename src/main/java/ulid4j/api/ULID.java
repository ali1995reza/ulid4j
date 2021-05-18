package ulid4j.api;

public interface ULID {

    long timestamp();

    int counter();

    int secure();

    String toStructureString();

}
