import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ulid4j.api.ULID;
import ulid4j.api.ULIDGenerator;
import ulid4j.impl.SimpleULIDGenerator;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ULIDTest {

    private ULIDGenerator generator;

    @BeforeAll
    public void initializeGenerator() {
        generator = new SimpleULIDGenerator(
                ByteBuffer.allocate(4).putInt(new SecureRandom().nextInt()).array() ,
                System::currentTimeMillis ,
                new SecureRandom()::nextInt
        );
    }

    @Test
    @DisplayName("Test collision and sort")
    public void testSortAndDuplication() {

        final int round = 10000000; //ten million tries
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


}
