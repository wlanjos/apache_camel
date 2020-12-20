package br.com.caelum.camel;

import java.text.SimpleDateFormat;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.dataformat.xstream.XStreamDataFormat;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.thoughtworks.xstream.XStream;


/*Quando já existe uma aplicação legada que precisa consumir um serviço web periodicamente e 
disponibilizar os dados desse serviço para a aplicação. Agora você já aprendeu como resolver isso.
*/

public class RotaHttpPollingNegociacoes {
	
	public static void main(String[] args) throws Exception {
		

		SimpleRegistry registro =  new SimpleRegistry();
		registro.put("mysql", criarDataSouce());

		
		
		final XStream xstream =  new XStream();
		xstream.alias("negociacao", Negociacao.class);
		
		CamelContext context = new DefaultCamelContext();
		
		context.addRoutes(new RouteBuilder() {

			@Override
			public void configure() throws Exception {
			    from("timer://negociacoes?fixedRate=true&delay=3s&period=360s")
			      .to("http4://argentumws.caelum.com.br/negociacoes")
			      .convertBodyTo(String.class)
			      .unmarshal(new XStreamDataFormat(xstream))
			      .split(body())
			      .process(new Processor() {

					@Override
					public void process(Exchange exchange) throws Exception {
						Negociacao negociacao = exchange.getIn().getBody(Negociacao.class);
						exchange.setProperty("preco", negociacao.getPreco());
						exchange.setProperty("quantidade", negociacao.getQuantidade());
						String data  = new SimpleDateFormat("YYY-MM-dd HH:mm:ss").format(negociacao.getData().getTime());
						exchange.setProperty("data", data);
					}
			      
			      })
			      .setBody(simple("insert into negociacao(preco,quantidade,data) value(${property.proco}, ${property.quantidade}, '${property.data}')"))
			      .log("${body}")
			      .delay(1000)
			   .to("jdbc.mysql")
			    .end(); //só deixa explícito que é o fim da rota
			}
		});
	}
	
	private static MysqlConnectionPoolDataSource criarDataSouce() {
		MysqlConnectionPoolDataSource mysqlDs =  new MysqlConnectionPoolDataSource();
		
		mysqlDs.setDatabaseName("camel");
		mysqlDs.setServerName("localhost");
		mysqlDs.setPort(3306);
		mysqlDs.setUser("root");
		mysqlDs.setPassword("root");
		return mysqlDs;
		
	}

}






















