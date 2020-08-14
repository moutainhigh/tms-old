package org.nw.web.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

/**
 * Computes and verifies digests of files and strings
 * 
 * 
 * @version $Revision: 1.1 $
 */
public class DigestUtil {

	/**
	 * Command line interface. Use -help for arguments.
	 * 
	 * @param args
	 *            the arguments passed in on the command line
	 */
	public static void main(String[] args) {

		String alg = "SHA";
		boolean file = false;

		if(args.length == 0 || args.length > 4) {
			printUsage();
			return;
		}

		for(int i = 0; i < args.length; i++) {
			String currArg = args[i].toLowerCase(Locale.US);
			if(currArg.equals("-help") || currArg.equals("-usage")) {
				printUsage();
				return;
			}
			if(currArg.equals("-alg")) {
				alg = args[i + 1];
			}
			if(currArg.equals("-file")) {
				file = true;
			}
		}

		if(file) {
			digestFile(args[args.length - 1], alg);
			return;
		} else {
			try {
				String hash = digestString(args[args.length - 1], alg);
				System.out.println("Hash is: " + hash);
				return;
			} catch(NoSuchAlgorithmException nsae) {
				System.out.println("No such algorithm available");
			}
		}
	}

	/**
	 * Print the command line usage string.
	 */
	public static void printUsage() {
		System.out.println("Usage: " + "java org.apache.james.security.DigestUtil" + " [-alg algorithm]"
				+ " [-file] filename|string");
	}

	/**
	 * Calculate digest of given file with given algorithm. Writes digest to
	 * file named filename.algorithm .
	 * 
	 * @param filename
	 *            the String name of the file to be hashed
	 * @param algorithm
	 *            the algorithm to be used to compute the digest
	 */
	public static void digestFile(String filename, String algorithm) {
		byte[] b = new byte[65536];
		int count = 0;
		int read = 0;
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			fis = new FileInputStream(filename);
			while(fis.available() > 0) {
				read = fis.read(b);
				md.update(b, 0, read);
				count += read;
			}
			byte[] digest = md.digest();
			StringBuffer fileNameBuffer = new StringBuffer(128).append(filename).append(".").append(algorithm);
			fos = new FileOutputStream(fileNameBuffer.toString());
			OutputStream encodedStream = MimeUtility.encode(fos, "base64");
			encodedStream.write(digest);
			fos.flush();
		} catch(Exception e) {
			System.out.println("Error computing Digest: " + e);
		} finally {
			try {
				fis.close();
				fos.close();
			} catch(Exception ignored) {
			}
		}
	}

	/**
	 * Calculate digest of given String using given algorithm. Encode digest in
	 * MIME-like base64.
	 * 
	 * @param pass
	 *            the String to be hashed
	 * @param algorithm
	 *            the algorithm to be used
	 * @return String Base-64 encoding of digest
	 * 
	 * @throws NoSuchAlgorithmException
	 *             if the algorithm passed in cannot be found
	 */
	public static String digestString(String pass, String algorithm) throws NoSuchAlgorithmException {

		MessageDigest md;
		ByteArrayOutputStream bos;

		try {
			md = MessageDigest.getInstance(algorithm);
			byte[] digest = md.digest(pass.getBytes("iso-8859-1"));
			bos = new ByteArrayOutputStream();
			OutputStream encodedStream = MimeUtility.encode(bos, "base64");
			encodedStream.write(digest);
			return bos.toString("iso-8859-1");
		} catch(IOException ioe) {
			throw new RuntimeException("Fatal error: " + ioe);
		} catch(MessagingException me) {
			throw new RuntimeException("Fatal error: " + me);
		}
	}

	/**
	 * Private constructor to prevent instantiation of the class
	 */
	private DigestUtil() {
	}
}
