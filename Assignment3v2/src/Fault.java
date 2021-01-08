/**
 * Each process has a fault type, describing its behavior when sending messages:
 *
 *  - NONE means a process will always send the correct value
 *  - NO_SEND means a process crashes from the start, not sending messages anymore
 *  - RANDOM_VAL means a process will send values randomly (imitating a faulty sensor).
 */
public enum Fault {
    NONE, NO_SEND, RANDOM_VAL
}
