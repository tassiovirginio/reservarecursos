package br.reservarecursos.ldap;

import br.reservarecursos.entities.Usuario;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * Created by tassio on 09/12/15.
 */
public class PersonRepo {

    private LdapTemplate ldapTemplate;

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }


    public List<Usuario> getAllPersonNamesToUsurios() {
        List<Usuario> retornoStringLdap = ldapTemplate.search(
                query().where("objectclass").is("person"),
                new AttributesMapper<Usuario>() {
                    @Override
                    public Usuario mapFromAttributes(Attributes attributes) throws NamingException {

                        Usuario usuario = new Usuario();
                        usuario.setCn(attributes.get("cn").get().toString());

                        Attribute sn = attributes.get("sn");
                        if(sn != null)usuario.setSn(sn.get().toString());

                        usuario.setObjectCategory(attributes.get("objectCategory").get().toString());


                        Attribute displayname = attributes.get("displayname");
                        if(displayname != null)usuario.setDisplayname(displayname.get().toString());

                        usuario.setDistinguishedname(attributes.get("distinguishedname").get().toString());

                        Attribute memberof = attributes.get("memberof");
                        if(memberof != null)usuario.setMemberof(memberof.get().toString());


                        Attribute description = attributes.get("description");
                        if(description != null)usuario.setDescription(description.get().toString());


                        usuario.setName(attributes.get("name").get().toString());


                        Attribute extensionAttribute1 = attributes.get("extensionAttribute1");
                        if(extensionAttribute1 != null)usuario.setExtensionAttribute1(extensionAttribute1.get().toString());
                        Attribute extensionAttribute2 = attributes.get("extensionAttribute2");
                        if(extensionAttribute2 != null)usuario.setExtensionAttribute2(extensionAttribute2.get().toString());
                        Attribute extensionAttribute10 = attributes.get("extensionAttribute10");
                        if(extensionAttribute10 != null)usuario.setExtensionAttribute10(extensionAttribute10.get().toString());

                        usuario.setObjectclass(attributes.get("objectclass").get().toString());

                        usuario.setNome(usuario.getDisplayname());
                        usuario.setLogin(usuario.getCn());


                        if(extensionAttribute10 != null && attributes.get("extensionAttribute10").get().toString().toUpperCase().equals("ativo")){
                            usuario.setAtivo(true);
                        }else{
                            usuario.setAtivo(false);
                        }

                        return usuario;
                    }
                });

        return retornoStringLdap;
    }

//    public List<String> getAllPersonNames_() {
//        return ldapTemplate.search(
//                query().where("objectclass").is("organizationalPerson"),
//                new AttributesMapper<String>() {
//                    @Override
//                    public String mapFromAttributes(Attributes attributes) throws NamingException {
//                        System.out.println(attributes);
//                        return attributes.get("cn").get().toString() + ":" + attributes.get("displayname").get().toString();
//                    }
//                });
//    }

    public boolean login(String username, String password){
        AndFilter filter = new AndFilter();
        filter
                .and(new EqualsFilter("objectclass", "person"))
                .and(new EqualsFilter("cn", username));

        return ldapTemplate.authenticate(DistinguishedName.EMPTY_PATH, filter.toString(), password);
    }

}
