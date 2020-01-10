package com.example.tutorial.tool;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Printer;

import io.grpc.MethodDescriptor.Marshaller;

/**
 * 使用JSON格式记录输入与输出日志
 * 
 */
public class JsonLoggerMarshaller<T extends Message> implements Marshaller<T> {

	private static final Logger LOG = Logger.getLogger(JsonLoggerMarshaller.class.getName());

	private Marshaller<T> baseMarshaller;

	private final Printer printer = JsonFormat.printer().omittingInsignificantWhitespace();

	public JsonLoggerMarshaller(Marshaller<T> baseMarshaller) {
		this.baseMarshaller = baseMarshaller;
	}

	public InputStream stream(T value) {
		try {
			// 记录输出日志
			String info = printer.print(value);
			LOG.info("output:" + info);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "do stream error", e);
		}

		return baseMarshaller.stream(value);
	}

	public T parse(InputStream stream) {
		T msg = baseMarshaller.parse(stream);

		try {
			// 记录输入日志
			String info = printer.print(msg);
			LOG.info("input:" + info);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "do parse error", e);
		}

		return msg;
	}
}
