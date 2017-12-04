package ch.epfl.sweng.groupup.object.account;

import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class MembersShould {
    private Member m1 = new Member("UUID", "XavierP", "Xavier", "Pantet", "xavier@pantet.ch", null);
    private Member m2 = new Member("UUID2", "XavierP", "Xavier", "Pantet", "xavier@pantet.ch", null);
    private Member m3 = new Member("UUID", "Xavier", "Xavier", "Pantet", "xavier@pantet.ch", null);
    private Member m4 = new Member("UUID", "XavierP", "Xavie", "Pantet", "xavier@pantet.ch", null);
    private Member m5 = new Member("UUID", "XavierP", "Xavier", "Pante", "xavier@pantet.ch", null);
    private Member m6 = new Member("UUID", "XavierP", "Xavier", "Pantet", "xavier@pantet.com", null);
    @Test
    public void beCreated(){
        Member m = m1;
    }

    @Test
    public void allowAccessToItsAttributes(){
        assertEquals(m1.getUUID().get(), "UUID");
        assertEquals(m1.getDisplayName().get(), "XavierP");
        assertEquals(m1.getGivenName().get(), "Xavier");
        assertEquals(m1.getFamilyName().get(), "Pantet");
        assertEquals(m1.getEmail().get(), "xavier@pantet.ch");
        assertTrue(m1.getLocation().isEmpty());
    }

    @Test
    public void allowToBeModified(){
        Member m = m1;
        m = m.withUUID("UUID2").withDisplayName("CedricM").withFirstName("Cedric").withLastName("Maire")
                .withEmail("cedric@maire.de").withLocation(null);
        assertEquals(m.getUUID().get(), "UUID2");
        assertEquals(m.getDisplayName().get(), "CedricM");
        assertEquals(m.getGivenName().get(), "Cedric");
        assertEquals(m.getFamilyName().get(), "Maire");
        assertEquals(m.getEmail().get(), "cedric@maire.de");
        assertTrue(m.getLocation().isEmpty());
    }

    @Test
    public void beImmutable(){
        assertFalse(m1.withUUID("UUID2") == m1);
        assertFalse(m1.withDisplayName("CedricM") == m1);
        assertFalse(m1.withFirstName("Cedric") == m1);
        assertFalse(m1.withLastName("Maire") == m1);
        assertFalse(m1.withEmail("cedric@maire.de") == m1);
    }

    @SuppressWarnings({"EqualsBetweenInconvertibleTypes", "EqualsWithItself"})
    @Test
    public void beEquatable(){
        assertTrue(m1.equals(m1));
        assertFalse(m1.equals(m2));
        assertFalse(m1.equals(m3));
        assertFalse(m1.equals(m4));
        assertFalse(m1.equals(m5));
        assertFalse(m1.equals(m6));
    }
}
