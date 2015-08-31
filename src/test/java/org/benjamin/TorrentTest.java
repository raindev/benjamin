package org.benjamin;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

@Test
public class TorrentTest {

    Bdecoder decoder;
    Bencoder encoder;
    Map<String, Object> torrent;
    ByteArrayOutputStream encodedTorrent;

    @BeforeMethod
    void setUp() {
        decoder = new Bdecoder(UTF_8, torrentFileStream());
        encodedTorrent = new ByteArrayOutputStream();
        encoder = new Bencoder(UTF_8, encodedTorrent);
    }

    @Test
    void decodeTorrent() throws IOException {
        torrent = decoder.readDictionary();
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

    @Test(dependsOnMethods = "decodeTorrent")
    void encodeTorrent() throws IOException {
        encoder.encode(torrent);
        assertEquals(
                IOUtils.toByteArray(torrentFileStream()),
                encodedTorrent.toByteArray(),
                "re-encoded torrent should be equal to original file");
    }

    InputStream torrentFileStream() {
        return getClass()
            .getResourceAsStream("/ubuntu-14.10-desktop-amd64.iso.torrent");
    }

}
