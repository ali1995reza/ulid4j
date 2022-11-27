
# ULID4J

A simple library to create ULID in java !


### Getting Started

To add library in your project you can simply add this dependency:

```xml

<dependency>
    <groupId>io.github.ali1995reza</groupId>
    <artifactId>ulid4j</artifactId>
    <version>0.1.1</version>
</dependency>
```


## ULID Structure

Structure of how a string represents a ULID is determined by implementations,
but there are some things to discus :

- **Time Sorted** :  ULID id must be time sorted, it means we have to 
    put time bits in [MSB](https://en.wikipedia.org/wiki/Bit_numbering).
- **Universally Unique** : ULID must be universally unique, it means 
    if 2 difference machines create ULIDs concurrently this ULIDs should be unique.
    We can use a machine-identifier after time bits to separate machines from each other.
    There are many identifiers like Node ID in clusters and in a wider world 
    we can use MAC address as machine-identifier.
- **Counted** : ULID id must be sorted even if 2 ULID created in a same time.
    For this problem we can use some counter bits after machine-identifier bits.
    If counter reset period be short, 32 bits can be enough.
- **Secure** : This is optional, maybe it's better a ULID be secure,
    it means no one knows what is the next ULID created on a machine.
    For this reason we can use a 32 bits random number and put it in
    [LBS](https://en.wikipedia.org/wiki/Bit_numbering).


## Java API

In this library we have 2 simple class :

### ULID

| Method Name          | Return Type     | Description                                            |
| :------------------- | :-------------- | :----------------------------------------------------- |
| `timestamp()`        | `long`          | return the timestamp that this ULID created in         |
| `counter()`          | `int`           | return the value of the counter when this ULID created |
| `secure()`           | `int`           | return the random secure value of this ULID            |
| `toStrutureString()` | `String`           | return the ULID as structured human-readable string    |

### ULIDGenerator

| Method Name          | Return Type     | Description                                            |
| :------------------- | :-------------- | :----------------------------------------------------- |
| `generate()`         | `ULID`          | return a new ULID                                      |
| `from(String from)`  | `ULID`          | return the ULID that reperesented from a String        |


## Implement

I just implement a Simple ULIDGenerator in this library.

### Specification

#### Structure

| 64 bits time millisecond | n-bits machine-identifier | 32 bits counter | 32 bits secure |
| :----------------------- | :------------------------ | :-------------- | :------------- |

In this implementation I just encode each **5** bits into a character.

for a `64 bits long` it will produce **13** characters.

for a `32 bits int` it will produce **7** characters.

.
.
.

### Example

#### Create a new ULIDGenerator

```
ULIDGenerator generator = new SimpleULIDGenerator(
                ByteBuffer.allocate(4).putInt(new SecureRandom().nextInt()).array() // machine-identifier ,
                System::currentTimeMillis // time milliseconds Supplier ,
                new SecureRandom()::nextInt // secure random Supplier
        );

```

### Generate a new ULID

```

ULID ulid = generator.generate();

```

### Represents ULID from a string format

```

ULID reperesented = generator.from("00002uc3404u0-914t3q0-001mmr296ebab1");

```


