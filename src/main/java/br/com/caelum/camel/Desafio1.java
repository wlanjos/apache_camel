package br.com.caelum.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class Desafio1 {
	
	public static void main(String[] args) throws Exception {
		
		CamelContext context =  new DefaultCamelContext();
		
		context.addRoutes(new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				from("timer://negociacoes?fixedRate=true&delay=1s&period=360s").
				to("http4://argentumws-spring.herokuapp.com/negociacoes").
				convertBodyTo(String.class).
				log("${body}").
				setHeader(Exchange.FILE_NAME, constant("negociacoes.xml")).
				to("file:saida");
			}
		});
		
	}

}
