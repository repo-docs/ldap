package com.example.ldap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.stereotype.Component;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.List;

@Component
public class LdapConnectionTester implements CommandLineRunner {

    @Autowired
    private LdapTemplate ldapTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Attempting to connect to LDAP using configured service account...");

        try {
            // This is a simple test to confirm the connection and authentication.
            // It tries to search for an object using the configured service account.
            // Replace 'cn=someuser' with an actual user DN or a filter that will return results
            // from your LDAP directory to get a more meaningful test.
            // For a basic connection test, any successful operation is sufficient.
            List<String> foundItems = ldapTemplate.search(
                    "", // Base DN for the search (empty string implies using the configured spring.ldap.base)
                    new EqualsFilter("objectClass", "organizationalUnit").encode(), // Example filter: searches for any organizational unit
                    // CORRECTED: The AttributesMapper lambda only takes one argument: Attributes attrs
                    (Attributes attrs) -> {
                        if (attrs != null) {
                            try {
                                // You can return any attribute or just null if you only care about successful search
                                // For example, returning the 'ou' attribute if it exists
                                if (attrs.get("ou") != null) {
                                    return (String) attrs.get("ou").get();
                                } else {
                                    return "Found Entry (no 'ou' attribute)"; // Or return a specific identifier
                                }
                            } catch (NamingException ex) {
                                // Log or handle error if attribute not found or other naming exception
                                System.err.println("Error mapping attributes: " + ex.getMessage());
                                return null;
                            }
                        }
                        return null;
                    }
            );

            if (foundItems != null) {
                System.out.println("LDAP connection and service account authentication successful!");
                if (!foundItems.isEmpty()) {
                    System.out.println("Found " + foundItems.size() + " organizational units as a test.");
                    // Optional: print the found items
                    // foundItems.forEach(System.out::println);
                } else {
                    System.out.println("No organizational units found with the test filter, but connection was successful.");
                }
            } else {
                System.out.println("LDAP connection was successful, but search returned null (might indicate no results or issue with the search logic).");
            }

        } catch (org.springframework.ldap.CommunicationException e) {
            System.err.println("LDAP connection FAILED! Check your `spring.ldap.urls` and server availability.");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (org.springframework.ldap.AuthenticationException e) {
            System.err.println("LDAP authentication FAILED! Check your `spring.ldap.username` and `spring.ldap.password` in application.properties.");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during LDAP connection test.");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}