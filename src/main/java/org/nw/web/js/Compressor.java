package org.nw.web.js;

import jargs.gnu.CmdLineParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * 1、使用YUIcompressor 压缩js文件,提供合并压缩多个Js文件到一个文件的功能 2、使用YUIcompressor 压缩Css文件,
 * 提供合并压缩多个Css文件到一个文件的功能
 */
public class Compressor {

	private static final Log logger = LogFactory.getLog(ScriptMergeCompressTag.class);
	private static final String CHARSET = "UTF-8";

	/**
	 * 合并压缩多个JS文件到一个文件,默认文件编码为UTF-8
	 * 
	 * @param fileList
	 *            　待压缩的JS文件列表
	 * @param outfile
	 *            压缩后的文件保存列表
	 */
	public static void compress(List<File> fileList, File outfile) {
		compress(fileList, outfile, CHARSET);
	}

	/**
	 * 合并压缩多个ｊｓ文件到一个文件
	 * 
	 * @param fileList
	 *            　待压缩的ｊｓ文件列表
	 * @param outfile
	 *            压缩后的文件保存列表
	 * @param encoding
	 *            文件编码
	 */
	public static void compress(List<File> fileList, File outfile, String encoding) {
		Writer out = null;
		try {
			out = new OutputStreamWriter(new FileOutputStream(outfile), encoding);
			for(File file : fileList) {
				logger.debug("开始压缩文件：" + file.getName());
				Compressor.compress((String.format("--type js --charset %s --preserve-semi %s", encoding,
						file.getPath())).split("\\s+"), out);
				logger.debug("文件：" + file.getName() + "压缩完成.");
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(out != null)
					out.close();
			} catch(IOException e) {
			}
		}
	}

	/**
	 * 合并压缩多个Css文件到一个文件,默认文件编码为GBK
	 * 
	 * @param fileList
	 *            　待压缩的Css文件列表
	 * @param outfile
	 *            压缩后的文件保存列表
	 */
	public static void compressCss(List<File> fileList, File outfile) {
		compressCss(fileList, outfile, CHARSET);
	}

	/**
	 * 合并压缩多个ｊｓ文件到一个文件
	 * 
	 * @param fileList
	 *            　待压缩的Css文件列表
	 * @param outfile
	 *            压缩后的文件保存列表
	 * @param encoding
	 *            文件编码
	 */
	public static void compressCss(List<File> fileList, File outfile, String encoding) {
		Writer out = null;
		try {
			out = new OutputStreamWriter(new FileOutputStream(outfile), encoding);
			for(File file : fileList) {
				logger.debug("开始压缩文件：" + file.getName());
				Compressor.compress(
						(String.format("--type css --charset %s %s", encoding, file.getPath())).split("\\s+"), out);
				logger.debug("文件：" + file.getName() + "压缩完成.");
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(out != null)
					out.close();
			} catch(IOException e) {
			}
		}
	}

	/**
	 * 压缩srcfile文件到outfile文件中
	 * 
	 * @param srcfile
	 *            　待压缩的ｊｓ文件
	 * @param outfile
	 *            压缩后的文件
	 * @param encoding
	 *            文件编码
	 */
	public static void compress(File srcfile, File outfile, String encoding) {
		Writer out = null;
		try {
			out = new OutputStreamWriter(new FileOutputStream(outfile), encoding);
			logger.debug("开始压缩文件：" + srcfile.getName());
			Compressor.compress(
					(String.format("--type js --charset %s --preserve-semi %s", encoding, srcfile.getPath()))
							.split("\\s+"), out);
			logger.debug("文件：" + srcfile.getName() + "压缩完成.");
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(out != null)
					out.close();
			} catch(IOException e) {
			}
		}

	}

	/**
	 * <ul>
	 * <li>linebreakpos=-1</li>
	 * <li>munge=true</li>
	 * <li>verbose=false</li>
	 * <li>preserveAllSemiColons=true</li>
	 * <li>disableOptimizations=false</li>
	 * </ul>
	 * 
	 * @param srcJs
	 *            待压缩的js片段
	 */
	// public static void compress(Writer writer, String srcJs) {
	// if(StringUtils.isBlank(srcJs)) {
	// return;
	// }
	// Reader in = new StringReader(srcJs);
	// try {
	// JavaScriptCompressor compressor = getJavaScriptCompressor(in);
	// compressor.compress(writer, -1, true, false, true, false);
	// } catch(EvaluatorException e) {
	// usage();
	// } catch(IOException e) {
	// e.printStackTrace();
	// } finally {
	//
	// if(in != null) {
	// try {
	// in.close();
	// } catch(IOException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// /*
	// * if (out != null) { try { out.close(); } catch (IOException e) {
	// * e.printStackTrace(); } }
	// */
	// }
	// }

	/**
	 * 修改com.yahoo.platform.yui.compressor.YUICompressor以方便ｊａｖａ内部调用
	 * 
	 * @param args
	 *            参见YUICompressor调用参数
	 * @param out
	 *            数据输出流，需要在外部关闭流
	 */
	private static void compress(String args[], Writer out) {
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option typeOpt = parser.addStringOption("type");
		CmdLineParser.Option verboseOpt = parser.addBooleanOption('v', "verbose");
		CmdLineParser.Option nomungeOpt = parser.addBooleanOption("nomunge");
		CmdLineParser.Option linebreakOpt = parser.addStringOption("line-break");
		CmdLineParser.Option preserveSemiOpt = parser.addBooleanOption("preserve-semi");
		CmdLineParser.Option disableOptimizationsOpt = parser.addBooleanOption("disable-optimizations");
		CmdLineParser.Option helpOpt = parser.addBooleanOption('h', "help");
		CmdLineParser.Option charsetOpt = parser.addStringOption("charset");
		CmdLineParser.Option outputFilenameOpt = parser.addStringOption('o', "output");

		Reader in = null;

		try {

			parser.parse(args);

			Boolean help = (Boolean) parser.getOptionValue(helpOpt);
			if(help != null && help.booleanValue()) {
				usage();

				// System.exit(0);
			}

			boolean verbose = parser.getOptionValue(verboseOpt) != null;

			String charset = (String) parser.getOptionValue(charsetOpt);
			if(charset == null || !Charset.isSupported(charset)) {
				charset = System.getProperty("file.encoding");
				if(charset == null) {
					charset = CHARSET;
				}
				if(verbose) {
					System.err.println("\n[INFO] Using charset " + charset);
				}
			}

			String[] fileArgs = parser.getRemainingArgs();
			String type = (String) parser.getOptionValue(typeOpt);

			if(fileArgs.length == 0) {

				if(type == null || !type.equalsIgnoreCase("js") && !type.equalsIgnoreCase("css")) {
					usage();
					// System.exit(1);
				}

				in = new InputStreamReader(System.in, charset);

			} else {

				if(type != null && !type.equalsIgnoreCase("js") && !type.equalsIgnoreCase("css")) {
					usage();
					// System.exit(1);
				}

				String inputFilename = fileArgs[0];

				if(type == null) {
					int idx = inputFilename.lastIndexOf('.');
					if(idx >= 0 && idx < inputFilename.length() - 1) {
						type = inputFilename.substring(idx + 1);
					}
				}

				if(type == null || !type.equalsIgnoreCase("js") && !type.equalsIgnoreCase("css")) {
					usage();
					// System.exit(1);
				}

				in = new InputStreamReader(new FileInputStream(inputFilename), charset);
			}

			int linebreakpos = -1;
			String linebreakstr = (String) parser.getOptionValue(linebreakOpt);
			if(linebreakstr != null) {
				try {
					linebreakpos = Integer.parseInt(linebreakstr, 10);
				} catch(NumberFormatException e) {
					usage();
					// System.exit(1);
				}
			}

			String outputFilename = (String) parser.getOptionValue(outputFilenameOpt);

			if(type.equalsIgnoreCase("js")) {

				try {

					JavaScriptCompressor compressor = getJavaScriptCompressor(in);
					// Close the input stream first, and then open the output
					// stream,
					// in case the output file should override the input file.
					in.close();
					in = null;

					if(outputFilename != null) {
						out = new OutputStreamWriter(new FileOutputStream(outputFilename), charset);
					}

					boolean munge = parser.getOptionValue(nomungeOpt) == null;
					boolean preserveAllSemiColons = parser.getOptionValue(preserveSemiOpt) != null;
					boolean disableOptimizations = parser.getOptionValue(disableOptimizationsOpt) != null;

					compressor.compress(out, linebreakpos, munge, verbose, preserveAllSemiColons, disableOptimizations);

				} catch(EvaluatorException e) {

					e.printStackTrace();
					// Return a special error code used specifically by the web
					// front-end.
					// System.exit(2);

				}

			} else if(type.equalsIgnoreCase("css")) {

				CssCompressor compressor = new CssCompressor(in);

				// Close the input stream first, and then open the output
				// stream,
				// in case the output file should override the input file.
				in.close();
				in = null;

				if(outputFilename != null) {
					out = new OutputStreamWriter(new FileOutputStream(outputFilename), charset);
				}

				compressor.compress(out, linebreakpos);
			}

		} catch(CmdLineParser.OptionException e) {

			usage();
			// System.exit(1);

		} catch(IOException e) {

			e.printStackTrace();
			// System.exit(1);

		} finally {

			if(in != null) {
				try {
					in.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}

			/*
			 * if (out != null) { try { out.close(); } catch (IOException e) {
			 * e.printStackTrace(); } }
			 */
		}
	}

	private static JavaScriptCompressor getJavaScriptCompressor(Reader in) throws IOException, EvaluatorException {
		JavaScriptCompressor compressor = new JavaScriptCompressor(in, new ErrorReporter() {
			public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
				if(line < 0) {
					System.err.println("\n[WARNING] " + message);
				} else {
					System.err.println("\n[WARNING] " + line + ':' + lineOffset + ':' + message);
				}
			}

			public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
				if(line < 0) {
					System.err.println("\n[ERROR] " + message);
				} else {
					System.err.println("\n[ERROR] " + line + ':' + lineOffset + ':' + message);
				}
			}

			public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource,
					int lineOffset) {
				error(message, sourceName, line, lineSource, lineOffset);
				return new EvaluatorException(message);
			}
		});
		return compressor;
	}

	private static void usage() {
		System.out.println("\nUsage: java -jar yuicompressor-x.y.z.jar [options] [input file]\n\n"

		+ "Global Options\n" + "  -h, --help                Displays this information\n"
				+ "  --type <js|css>           Specifies the type of the input file\n"
				+ "  --charset <charset>       Read the input file using <charset>\n"
				+ "  --line-break <column>     Insert a line break after the specified column number\n"
				+ "  -v, --verbose             Display informational messages and warnings\n"
				+ "  -o <file>                 Place the output into <file>. Defaults to stdout.\n\n"

				+ "JavaScript Options\n" + "  --nomunge                 Minify only, do not obfuscate\n"
				+ "  --preserve-semi           Preserve all semicolons\n"
				+ "  --disable-optimizations   Disable all micro optimizations\n\n"

				+ "If no input file is specified, it defaults to stdin. In this case, the 'type'\n"
				+ "option is required. Otherwise, the 'type' option is required only if the input\n"
				+ "file extension is neither 'js' nor 'css'.");
	}

	public static void compress(String code, Writer writer) {
		Reader in = null;
		try {
			in = new InputStreamReader(IOUtils.toInputStream(code));
			JavaScriptCompressor compressor = new JavaScriptCompressor(in, new ErrorReporter() {
				public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
					if(line < 0) {
						System.err.println("/n[WARNING] " + message);
					} else {
						System.err.println("/n[WARNING] " + line + ':' + lineOffset + ':' + message);
					}
				}

				public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
					if(line < 0) {
						System.err.println("/n[ERROR] " + message);
					} else {
						System.err.println("/n[ERROR] " + line + ':' + lineOffset + ':' + message);
					}
				}

				public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource,
						int lineOffset) {
					error(message, sourceName, line, lineSource, lineOffset);
					return new EvaluatorException(message);
				}
			});
			compressor.compress(writer, -1, true, false, false, false);
		} catch(Exception e) {
			logger.error("yui js compress error:", e);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	public static void main(String[] args) throws IOException, UnsupportedEncodingException {

		// String outfile = "E:\\script\\compress_ftl.js";
		//
		// List<File> fileList = new ArrayList<File>();
		//
		// fileList.add(new File("E:\\script\\bill-marco.ftl"));
		// // fileList.add(new File("E:\\script\\ext-all.css"));
		// // fileList.add(new File("E:\\script\\ext-patch.css"));
		//
		// Compressor.compress(fileList, new File(outfile), CHARSET);
		PrintWriter writer = new PrintWriter(System.out);
		String code = "function func(){alert(1);}";
		Compressor.compress(code, writer);
	}
}
