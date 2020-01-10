package com.example.tutorial.tool;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.Message;

import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.Marshaller;
import io.grpc.MethodDescriptor.PrototypeMarshaller;

public class GlobalMarshallerInitializer {

	private static final Logger LOG = Logger.getLogger(GlobalMarshallerInitializer.class.getName());

	// 为记录json格式的日志，注册JsonLoggerMarshaller
	public static void initiateJsonLoggerMarshaller(Class<?> serviceClass) {
		try {
			Field serviceNameField = serviceClass.getField("SERVICE_NAME");
			if (serviceNameField == null) {
				return;
			}

			for (Method m : serviceClass.getDeclaredMethods()) {

				if (Modifier.isStatic(m.getModifiers()) && m.getReturnType() == MethodDescriptor.class) {

					// 为各RPC调用方法生成默认的MethodDescriptor，默认MethodDescriptor中的Marshaller使用的是二进制格式传输报文
					MethodDescriptor<?, ?> md = (MethodDescriptor<?, ?>) m.invoke(null);

					Field requestMarshallerField = md.getClass().getDeclaredField("requestMarshaller");
					Field responseMarshallerField = md.getClass().getDeclaredField("responseMarshaller");

					// 使用反射机制设置输入与输出Marshaller
					requestMarshallerField.setAccessible(true);
					requestMarshallerField.set(md, new JsonLoggerMarshaller(md.getRequestMarshaller()));
					requestMarshallerField.setAccessible(false);

					responseMarshallerField.setAccessible(true);
					responseMarshallerField.set(md, new JsonLoggerMarshaller(md.getResponseMarshaller()));
					responseMarshallerField.setAccessible(false);

					LOG.info("finish to initiate method: " + md.getFullMethodName());
				}
			}
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "fail to initiate JsonLoggerMarshaller " + serviceClass.getName(), e);
		}
	}

	// 注册定制的Marshaller
	public static void initiateMarshaller(Class<?> serviceClass,
			Class<? extends Marshaller<? extends Message>> marshallerClass) {
		try {
			Field serviceNameField = serviceClass.getField("SERVICE_NAME");
			if (serviceNameField == null) {
				return;
			}

			for (Method m : serviceClass.getDeclaredMethods()) {

				if (Modifier.isStatic(m.getModifiers()) && m.getReturnType() == MethodDescriptor.class) {

					// 为各RPC调用方法生成默认的MethodDescriptor，默认MethodDescriptor中的Marshaller使用的是二进制格式传输报文
					MethodDescriptor<?, ?> md = (MethodDescriptor<?, ?>) m.invoke(null);

					Field requestMarshallerField = md.getClass().getDeclaredField("requestMarshaller");
					Field responseMarshallerField = md.getClass().getDeclaredField("responseMarshaller");

					Object requestMessagePrototype = ((PrototypeMarshaller<?>) md.getRequestMarshaller())
							.getMessagePrototype();
					Object responseMessagePrototype = ((PrototypeMarshaller<?>) md.getResponseMarshaller())
							.getMessagePrototype();

					Marshaller<? extends Message> requestMarshallerInstance = marshallerClass
							.getConstructor(Message.class).newInstance(requestMessagePrototype);
					Marshaller<? extends Message> responseMarshallerInstance = marshallerClass
							.getConstructor(Message.class).newInstance(responseMessagePrototype);

					// 使用反射机制设置输入Marshaller
					requestMarshallerField.setAccessible(true);
					requestMarshallerField.set(md, requestMarshallerInstance);
					requestMarshallerField.setAccessible(false);

					// 使用反射机制设置输出Marshaller
					responseMarshallerField.setAccessible(true);
					responseMarshallerField.set(md, responseMarshallerInstance);
					responseMarshallerField.setAccessible(false);

					LOG.info("finish to initiate method: " + md.getFullMethodName());
				}
			}
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "fail to initiate service " + serviceClass.getName(), e);
		}
	}
}
