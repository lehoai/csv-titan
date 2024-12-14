package lehoai.csvtitan.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * A utility class for merging two large CSV files into one.
 * This class improves performance by handling files at the byte level rather than reading line by line,
 * while ensuring that headers are managed correctly.
 */
public class CsvUnion {

    /**
     * The character encoding to use when reading and writing the CSV files.
     */
    private final String encode;

    /**
     * Constructs a new {@code CsvUnion} instance with the specified character encoding.
     *
     * @param encode the character encoding to use
     */
    public CsvUnion(String encode) {
        this.encode = encode;
    }

    /**
     * Merges two CSV files into one output file.
     * The header from the first file is included, while the header from the second file is skipped.
     *
     * @param file1   the path to the first CSV file
     * @param file2   the path to the second CSV file
     * @param outfile the path to the output file
     * @throws IOException if an I/O error occurs during file processing
     */
    public void union(String file1, String file2, String outfile) throws IOException {
        FileInputStream fis1 = new FileInputStream(file1);
        FileInputStream fis2 = new FileInputStream(file2);
        FileOutputStream fos = new FileOutputStream(outfile);

        // Read the first file and include its header
        copyStream(fis1, fos);
        // write new line
        fos.write(System.lineSeparator().getBytes(this.encode));

        // Read the second file and skip its header
        BufferedReader br = new BufferedReader(new InputStreamReader(fis2, Charset.forName(this.encode)));
        String header = br.readLine(); // Read and discard the header
        br.close();
        fis2.close();

        if (header != null) {
            fis2 = new FileInputStream(file2);
            int headerLength = header.getBytes(this.encode).length + System.lineSeparator().getBytes(this.encode).length;

            // Skip the header in the second file
            if (fis2.skip(headerLength) != headerLength) {
                throw new IOException("Skipping header " + header + " of " + headerLength + " bytes failed");
            }
            copyStream(fis2, fos);
        }

        // Flush and close resources
        fos.flush();
        fis1.close();
        fis2.close();
    }

    /**
     * Copies the contents of an input stream to an output stream in chunks for performance.
     * Flushes the output periodically to ensure smooth processing for large files.
     *
     * @param is the input stream to read from
     * @param os the output stream to write to
     * @throws IOException if an I/O error occurs during the copy
     */
    private void copyStream(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[8192]; // 8 KB buffer for efficient transfer
        int bytesRead;
        int step = 0;

        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
            step++;

            // Flush periodically to avoid large memory usage
            if (step == 100) {
                os.flush();
            }
        }
    }
}