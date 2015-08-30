package org.benjamin;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import java.io.InputStream;
import java.io.IOException;
import java.util.Map;

import static java.nio.charset.StandardCharsets.US_ASCII;
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
        decoder = new Bdecoder(US_ASCII, stream);
    }

    @Test
    @SuppressWarnings("unchecked")
    void decodeTorrent() throws IOException {
        Map<String, ?> torrent = decoder.readDictionary();
        assertEquals(torrent.get("announce"), "http://torrent.ubuntu.com:6969/announce"
                .getBytes("UTF-8"));
        assertEquals(torrent.get("comment"), "Ubuntu CD releases.ubuntu.com"
                .getBytes("UTF-8"));
        assertReflectionEquals(
                torrent.get("announce-list"),
                asList(
                    // first announce tier
                    asList("http://torrent.ubuntu.com:6969/announce"
                        .getBytes("UTF-8")),
                    // backup announce tier
                    asList("http://ipv6.torrent.ubuntu.com:6969/announce"
                        .getBytes("UTF-8"))));
        assertEquals(torrent.get("creation date"), 1414070124L);    // 10.23.2014 1:15pm

        Map<String, ?> info = (Map<String, ?>) torrent.get("info");
        assertEquals(info.get("length"), 1162936320L);  // 1.08 Gb
        assertEquals(info.get("name"), "ubuntu-14.10-desktop-amd64.iso"
                .getBytes("UTF-8"));
        assertEquals(info.get("piece length"), 524288L);
        // concatenation of hashsums of the file pieces
        assertEquals(((byte[]) info.get("pieces")).length, 44380);
    }
}
