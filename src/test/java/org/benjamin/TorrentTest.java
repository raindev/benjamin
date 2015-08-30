package org.benjamin;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import java.io.InputStream;
import java.io.IOException;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

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
        Map<String, Object> torrent = decoder.readDictionary();
        assertEquals(torrent.get("announce"), "http://torrent.ubuntu.com:6969/announce"
                .getBytes(UTF_8));
        assertEquals(torrent.get("comment"), "Ubuntu CD releases.ubuntu.com"
                .getBytes(UTF_8));
        assertReflectionEquals(
                torrent.get("announce-list"),
                asList(
                    // first announce tier
                    asList("http://torrent.ubuntu.com:6969/announce"
                        .getBytes(UTF_8)),
                    // backup announce tier
                    asList("http://ipv6.torrent.ubuntu.com:6969/announce"
                        .getBytes(UTF_8))));
        assertEquals(torrent.get("creation date"), 1414070124L);    // 10.23.2014 1:15pm

        @SuppressWarnings("unchecked")
        Map<String, Object> info = (Map<String, Object>) torrent.get("info");
        assertEquals(info.get("length"), 1162936320L);  // 1.08 Gb
        assertEquals(info.get("name"), "ubuntu-14.10-desktop-amd64.iso"
                .getBytes(UTF_8));
        assertEquals(info.get("piece length"), 524288L);
        // concatenation of hash sums of the file pieces
        assertEquals(((byte[]) info.get("pieces")).length, 44380);
    }
}
