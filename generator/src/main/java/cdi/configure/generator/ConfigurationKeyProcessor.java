package cdi.configure.generator;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;

import cdi.configure.ConfigurationKey;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({ "cdi.configure.ConfigurationKey" })
public class ConfigurationKeyProcessor extends AbstractProcessor {

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		processingEnv.getMessager().printMessage(Kind.NOTE, "called getSupportedAnnotationTypes");
		return Sets.newHashSet(ConfigurationKey.class.getName());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		processingEnv.getMessager().printMessage(Kind.NOTE, "called getSupportedSourceVersion");
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		// TODO Auto-generated method stub
		processingEnv.getMessager().printMessage(Kind.NOTE, "called processing");
		Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(ConfigurationKey.class);
		if (elementsAnnotatedWith.size() > 1) {
			processingEnv.getMessager().printMessage(Kind.ERROR, "more than one element per type ConfigurationKey");
		}
		for (Element e : elementsAnnotatedWith) {
			if (e.getKind() == ElementKind.ENUM) {
				try {
					JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile("ConfigurationBean", e);
					
					String code = "public class ConfigurationBean{}";
					Writer writer = sourceFile.openWriter();
					writer.write(code);
					writer.flush();
					writer.close();
				} catch (IOException e1) {
					processingEnv.getMessager().printMessage(Kind.ERROR, "error creating file", e);
				}

			} else {
				processingEnv.getMessager().printMessage(Kind.ERROR, "not an enum type", e);
			}
		}

		return true;
	}

}
