import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.function.Executable;
import ulid4j.api.ULID;
import ulid4j.api.ULIDGenerator;
import ulid4j.api.exceptions.ULIDFormatException;
import ulid4j.impl.SimpleULIDGenerator;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ULIDTest {

    private final ULIDGenerator generator;

    public ULIDTest() {
        generator = new SimpleULIDGenerator(
                ByteBuffer.allocate(4).putInt(new SecureRandom().nextInt()).array() ,
                System::currentTimeMillis ,
                new SecureRandom()::nextInt
        );
    }

    @Test
    @DisplayName("Test collision and sort")
    public void testSortAndDuplication() {

        final int round = 1000000; //ten million tries
        ULID ulid = generator.generate();

        for(int i=0;i<round;i++) {
            ULID newUlid = generator.generate();
            assertThat(newUlid.toString()).isGreaterThan(ulid.toString());
            ULID from = generator.from(newUlid.toString());
            assertThat(newUlid.timestamp()).isEqualTo(from.timestamp());
            assertThat(newUlid.counter()).isEqualTo(from.counter());
            assertThat(newUlid.secure()).isEqualTo(from.secure());
            ulid = newUlid;
        }
    }

    @Test
    @DisplayName("Test validation of string id")
    public void testValidationStringFormat() {
        assertDoesNotThrow(()->{
            ULID withId = generator.from("00002uc3sksfe-0681-0047qo10tbl7t0");
            ULID withoutId = generator.from("00002uc3sksfe-0047qo10tbl7t0");
            assertThat(withId.timestamp()).isEqualTo(withoutId.timestamp());
            assertThat(withId.counter()).isEqualTo(withoutId.counter());
            assertThat(withId.secure()).isEqualTo(withoutId.secure());
        });
        assertThrows(ULIDFormatException.class, ()-> generator.from("00002zc3sksfe-0681-0047qo10tbl7t0")); // illegal char error
        assertThrows(ULIDFormatException.class, ()-> generator.from("00002zc3sksfe-0047qo0tbl7t0")); //size error
        assertThrows(ULIDFormatException.class, ()-> generator.from("00002uc3sksfe0047qo10tbl7t00")); //no separator -
        assertThrows(ULIDFormatException.class, ()-> generator.from("00002سc3sksfe-0681-0047qo10tbl7t0")); //none-ascii character
    }


}
