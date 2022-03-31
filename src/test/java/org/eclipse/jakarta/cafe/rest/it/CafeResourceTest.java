package org.eclipse.jakarta.cafe.rest.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import org.eclipse.jakarta.cafe.JaxrsActivator;
import org.eclipse.jakarta.cafe.model.CafeRepository;
import org.eclipse.jakarta.cafe.model.entity.Coffee;
import org.eclipse.jakarta.cafe.rest.CafeResource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class CafeResourceTest {

	@PersistenceContext
	private EntityManager entityManager;

	@Inject
	private UserTransaction transaction;

	@ArquillianResource
	private URL base;

	@Deployment
	public static WebArchive createDeployment() {
		return ShrinkWrap.create(WebArchive.class)
				.addClass(CafeRepository.class)
				.addClass(Coffee.class)
				.addClasses(CafeResource.class, JaxrsActivator.class)
				.addAsResource("test-persistence.xml", "META-INF/persistence.xml")
				// Enable CDI
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@Test
	public void testCreateCoffee() throws MalformedURLException {
		Coffee coffee = null;

		TypedQuery<Coffee> query = entityManager.createQuery("SELECT o FROM Coffee o WHERE o.name = :name",
				Coffee.class);
		query.setParameter("name", "Test-A");

		try {
			coffee = query.getSingleResult();
			fail("No entity should have been found.");
		} catch (NoResultException ne) {
			// Expected
		}

		coffee = new Coffee("Test-A", 7.25);
		ClientBuilder.newClient().target(new URL(base, "rest/coffees").toExternalForm()).request(MediaType.APPLICATION_JSON).post(Entity.json(coffee));

		query = entityManager.createQuery("SELECT o FROM Coffee o WHERE o.name = :name", Coffee.class);
		query.setParameter("name", "Test-A");

		coffee = query.getSingleResult();

		assertNotNull(coffee);
		assertEquals(coffee.getName(), "Test-A");
		assertEquals(coffee.getPrice().doubleValue(), 7.25, 0);
	}

	@Test
	public void testGetCoffeeById() throws NotSupportedException, SystemException, SecurityException,
			IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException, MalformedURLException {
		transaction.begin();
		Coffee coffee = new Coffee("Test-B", 5.99);
		entityManager.persist(coffee);
		transaction.commit();

		coffee = ClientBuilder.newClient().target(new URL(base, "rest/coffees").toExternalForm()).path(coffee.getId().toString())
				.request(MediaType.APPLICATION_JSON).get(Coffee.class);

		assertNotNull(coffee);
		assertEquals(coffee.getName(), "Test-B");
		assertEquals(coffee.getPrice().doubleValue(), 5.99, 0);
	}

	@Test
	public void testGetAllCoffees() throws NotSupportedException, SystemException, SecurityException,
			IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException, MalformedURLException {
		transaction.begin();
		Coffee coffee = new Coffee("Test-C", 4.75);
		entityManager.persist(coffee);
		coffee = new Coffee("Test-D", 1.99);
		entityManager.persist(coffee);
		coffee = new Coffee("Test-E", 2.95);
		entityManager.persist(coffee);
		transaction.commit();

		List<Coffee> coffees = ClientBuilder.newClient().target(new URL(base, "rest/coffees").toExternalForm()).request(MediaType.APPLICATION_JSON)
				.get(new GenericType<List<Coffee>>() {
				});

		assertEquals(coffees.size(), 3);
		assertEquals(coffees.get(0).getName(), "Test-C");
		assertEquals(coffees.get(0).getPrice().doubleValue(), 4.75, 0);
		assertEquals(coffees.get(1).getName(), "Test-D");
		assertEquals(coffees.get(1).getPrice().doubleValue(), 1.99, 0);
		assertEquals(coffees.get(2).getName(), "Test-E");
		assertEquals(coffees.get(2).getPrice().doubleValue(), 2.95, 0);
	}

	@Test
	public void testDeleteCoffee() throws NotSupportedException, SystemException, SecurityException,
			IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException, MalformedURLException {
		transaction.begin();
		Coffee coffee = new Coffee("Test-Z", 7.77);
		entityManager.persist(coffee);
		transaction.commit();

		ClientBuilder.newClient().target(new URL(base, "rest/coffees").toExternalForm()).path(coffee.getId().toString()).request().delete();

		TypedQuery<Coffee> query = entityManager.createQuery("SELECT o FROM Coffee o WHERE o.name = :name",
				Coffee.class);
		query.setParameter("name", "Test-Z");

		try {
			coffee = query.getSingleResult();
			fail("No entity should have been found.");
		} catch (NoResultException ne) {
			// Expected
		}
	}

	@After
	public void deleteCoffees() throws NotSupportedException, SystemException, SecurityException, IllegalStateException,
			RollbackException, HeuristicMixedException, HeuristicRollbackException {
		transaction.begin();
		entityManager.createQuery("DELETE FROM Coffee").executeUpdate();
		transaction.commit();
	}
}
