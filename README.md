# JPersistence

Biblioteca java que que visa simplificar o processo de persistência de dados

### Iniciando

Gerar a biblioteca JAR e instalar no repositório local:

    $ git clone git://github.com/henrique-gouveia/jpersistence.git
    $ cd jpersistence
    $ mvn install

Adicionar a dependência no pom.xml:

    <dependency>
      <groupId>br.com.ndevfactory</groupId>
      <artifactId>jpersistence</artifactId>
      <version>1.0.0-SNAPSHOT</version>
      <scope>compile</scope>
    </dependency>

### Configuração

Para configuração através do arquivo `persistence_config.properties`, esse deverá ser criado no projeto que faz uso da biblioteca com
os seguintes pares de chave e valor:

    persistence.persistence_unit_name=[persistence_unit_name]

A biblioteca faz uso de CDI com a implementação `Weld` que pode ser baixado em http://weld.cdi-spec.org/ ou simplesmente adicionar a
dependência no pom.xml:

    <!-- Weld (implementação do CDI) -->
    <dependency>
      <groupId>org.jboss.weld.servlet</groupId>
      <artifactId>weld-servlet</artifactId>
      <version>2.2.9.Final</version>
      <scope>compile</scope>
    </dependency>
    <!-- Weld depende do Jandex -->
    <dependency>
      <groupId>org.jboss</groupId>
      <artifactId>jandex</artifactId>
      <version>1.2.3.Final</version>
      <scope>compile</scope>
    </dependency>
    

Crie um arquivo chamado `context.xml` no diretório `src/main/webapp/META-INF` do projeto com as seguintes configurações:

    <?xml version="1.0" encoding="UTF-8"?>
    <Context>
      <Manager pathname=""/>
      <Resource name="BeanManager" auth="Container"
        type="javax.enterprise.inject.spi.BeanManager"
        factory="org.jboss.weld.resources.ManagerObjectFactory"/>
    </Context>    

No arquivo `web.xml`, adicionar o seguinte código:

    <listener>
      <listener-class>
        org.jboss.weld.environment.servlet.Listener
      </listener-class>
    </listener>
    <resource-env-ref>
      <resource-env-ref-name>BeanManager</resource-env-ref-name>
      <resource-env-ref-type>
        javax.enterprise.inject.spi.BeanManager
      </resource-env-ref-type>
    </resource-env-ref>
    
Por fim, para habilitar CDI no projeto adicione um arquivo vazio chamado `beans.xml` no diretório `src/main/resources/META-INF`.

EntityManager não é um bean CDI, por tanto, não se pode injetá-lo automaticamente. Para tanto, se faz necessário criar um `Produtor` 
de EntityManager que gerará um objeto que poderá ser injetado, a seguir um exemplo:

    package br.com.example.producer;
    
    import javax.enterprise.context.ApplicationScoped;
    import javax.enterprise.context.RequestScoped;
    import javax.enterprise.inject.Disposes;
    import javax.enterprise.inject.Produces;
    import javax.persistence.EntityManager;
    import javax.persistence.EntityManagerFactory;
    import javax.persistence.Persistence;
    
    import br.com.ndevfactory.persistence.PersistenceConfig;
    import br.com.ndevfactory.persistence.PersistenceConfigBuilder;
    
    @ApplicationScoped
    public class EntityManagerProducer {
    	
    	private EntityManagerFactory factory;
    	private PersistenceConfig config;
    
    	public EntityManagerProducer() {
    		this.config = new PersistenceConfigBuilder().fromBundle().build();
    		this.factory = Persistence.createEntityManagerFactory(this.config.getPersistenceUnitName());
    	}
    	
    	@Produces
    	@RequestScoped
    	public EntityManager createEntityManager() {
    		return this.factory.createEntityManager();
    	}
    	
    	public void closeEntityManager(@Disposes EntityManager manager) {
    		manager.close();
    	}
    	
    }
    

A biblioteca já fornece e faz uso de uma anotação denominada `@Transactional` que associa um método ou classe que será interceptada 
por um  interceptador responsável pelo controle de transação, a seguir um exemplo:

    package br.com.example.producer;
    
    import java.io.Serializable;
    
    import javax.annotation.Priority;
    import javax.inject.Inject;
    import javax.interceptor.AroundInvoke;
    import javax.interceptor.Interceptor;
    import javax.interceptor.InvocationContext;
    import javax.persistence.EntityManager;
    import javax.persistence.EntityTransaction;
    
    import br.com.ndevfactory.persistence.annotation.Transactional;
    
    @Priority(Interceptor.Priority.LIBRARY_BEFORE)
    @Interceptor
    @Transactional
    public class TransactionInterceptor implements Serializable {
    
    	private static final long serialVersionUID = 1L;
    
    	@Inject 
    	private EntityManager entityManager;
    	
    	@AroundInvoke
    	public Object invoke(InvocationContext context) throws Exception {
    		EntityTransaction transaction = entityManager.getTransaction();
    		boolean done = false;
    		
    		try {
    			if (!transaction.isActive()) {
    				// truque para fazer rollback no que já passou
    				// (senão, um futuro commit, confirmaria até mesmo 
    				// operacões sem transação
    				transaction.begin();
    				transaction.rollback();
    				
    				// Agora sim, inicia a transação
    				transaction.begin();
    				
    				done = true;
    			}
    			
    			return context.proceed();
    			
    		} catch (Exception e) {
    			if (transaction != null && done) {
    				transaction.rollback();
    			}
    
    			throw e;
    		} finally {
    			if (transaction != null && transaction.isActive() && done) {
    				transaction.commit();
    			}
    		}
    	}
    }

Deve-se registrar o interceptor, uma forma de fazer isso é no arquivo `beans.xml` com as seguintes configurações:

    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://xmlns.jcp.org/xml/ns/javaee"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
          http://xmlns.jcp.org/xml/ns/javaee/beans_1_1.xsd"
          version="1.1" bean-discovery-mode="all">
        <interceptors>
          <class>
            br.com.example.TransactionInterceptor
          </class>
        </interceptors>
    </beans>
    
### Uso

No projeto que faz uso da biblioteca, duas classes deverão ser extendidas, `AbstractRepository<T extends Object>` que contém os médos
`boolean contains(T entity)`, `Long count(T entity)`, `List<T> findAll()`, `T findOne(Object id)`, `boolean insert(T entity)`, 
`boolean update(T entity)` e `boolean delete(T entity)` com implementações bases, bem como, `AbstractService<T extends Object>` que
faz uso de um respository já com injeção configurada e que contém métodos de CRUD já implementados.
