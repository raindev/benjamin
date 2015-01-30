package org.benjamin;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import java.io.InputStream;
import java.io.IOException;
import java.util.Map;

import static org.testng.Assert.*;
import static java.nio.charset.StandardCharsets.UTF_8;

@Test
public class TorrentTest {

    InputStream stream;
    Bdecoder decoder;

    @BeforeMethod
    void setUp() {
        stream = getClass().getResourceAsStream("/ubuntu-14.10-desktop-amd64.iso.torrent");
        decoder = new Bdecoder(UTF_8, stream);
    }

    @Test
    void decodeTorrent() throws IOException {
        Map<String, ?> torrent = decoder.readDictionary();

        assertEquals(torrent.get("announce"), "http://torrent.ubuntu.com:6969/announce");
        assertEquals(torrent.get("comment"), "Ubuntu CD releases.ubuntu.com");
    }
}
