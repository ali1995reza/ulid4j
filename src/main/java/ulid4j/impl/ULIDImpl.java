package ulid4j.impl;

import ulid4j.api.ULID;

import java.util.Objects;

final class ULIDImpl implements ULID {


    private final long timestamp;
    private final int counter;
    private final int secure;
    private final String stringValue;

    public ULIDImpl(long timestamp, int counter, int secure, String stringValue) {
        this.timestamp = timestamp;
        this.counter = counter;
        this.secure = secure;
        this.stringValue = stringValue;
    }


    @Override
    public long timestamp() {
        return 0;
    }

    @Override
    public int counter() {
        return 0;
    }

    @Override
    public int secure() {
        return 0;
    }

    @Override
    public String toStructureString() {
        return "ULID {" + "timestamp=" + timestamp + ", counter=" + counter + ", secure=" + secure +
                ", stringValue='" + stringValue + '\'' + '}';
    }

    @Override
    public String toString() {
        return stringValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ULIDImpl other = (ULIDImpl) o;

        return this.stringValue.equals(other.stringValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, counter, secure, stringValue);
    }

}
