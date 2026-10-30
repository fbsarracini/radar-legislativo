package br.com.radarlegislativo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UseCase {
}
