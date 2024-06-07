package net.HeChu.Common;

import java.util.UUID;

public interface Message {
    UUID getUUID();
    UUID getChatUUID();
    int getSender();
}
