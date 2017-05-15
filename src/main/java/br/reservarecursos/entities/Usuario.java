/**
 * Copyright 2015 - Tássio Guerreiro Antunes Virgínio
 *
 * Este arquivo é parte do programa Reserva de Recursos
 *
 * O Reserva de Recursos é um software livre; você pode redistribui-lo e/ou modifica-lo
 * dentro dos termos da Licença Pública Geral GNU como publicada pela
 * Fundação do Software Livre (FSF); na versão 2 da Licença.
 *
 * Este programa é distribuido na esperança que possa ser util, mas SEM
 * NENHUMA GARANTIA; sem uma garantia implicita de ADEQUAÇÂO a qualquer
 * MERCADO ou APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU
 * para maiores detalhes.
 *
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU, sob o
 * título "licensa_uso.htm", junto com este programa, se não, escreva para a
 * Fundação do Software Livre(FSF) Inc., 51 Franklin St, Fifth Floor,
 */
package br.reservarecursos.entities;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by tassio on 26/11/15.
 */
@Entity
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    private String cn;

    private String sn;

    private String displayname;

    private String description;

    private String distinguishedname;

    private String memberof;

    private String name;

    private String extensionAttribute1;

    private String extensionAttribute2;

    private String extensionAttribute10;

    private String objectCategory;

    private String objectclass;

    private String nome;

    private String login;

    private String senha;

    private String email;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean admin = false;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean adminGeral = false;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean adminEmprestimo = false;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean ativo  = false;

    @ManyToOne
    private Grupo grupo;

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Boolean getAdminEmprestimo() {
        return adminEmprestimo;
    }

    public void setAdminEmprestimo(Boolean adminEmprestimo) {
        this.adminEmprestimo = adminEmprestimo;
    }

    public String getObjectCategory() {
        return objectCategory;
    }

    public void setObjectCategory(String objectCategory) {
        this.objectCategory = objectCategory;
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDistinguishedname() {
        return distinguishedname;
    }

    public void setDistinguishedname(String distinguishedname) {
        this.distinguishedname = distinguishedname;
    }

    public String getMemberof() {
        return memberof;
    }

    public void setMemberof(String memberof) {
        this.memberof = memberof;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtensionAttribute1() {
        return extensionAttribute1;
    }

    public void setExtensionAttribute1(String extensionAttribute1) {
        this.extensionAttribute1 = extensionAttribute1;
    }

    public String getExtensionAttribute2() {
        return extensionAttribute2;
    }

    public void setExtensionAttribute2(String extensionAttribute2) {
        this.extensionAttribute2 = extensionAttribute2;
    }

    public String getExtensionAttribute10() {
        return extensionAttribute10;
    }

    public void setExtensionAttribute10(String extensionAttribute10) {
        this.extensionAttribute10 = extensionAttribute10;
    }

    public String getObjectclass() {
        return objectclass;
    }

    public void setObjectclass(String objectclass) {
        this.objectclass = objectclass;
    }

    public Boolean getAdminGeral() {
        return adminGeral;
    }

    public void setAdminGeral(Boolean adminGeral) {
        this.adminGeral = adminGeral;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Usuario usuario = (Usuario) o;

        if (!id.equals(usuario.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", cn='" + cn + '\'' +
                ", sn='" + sn + '\'' +
                ", displayname='" + displayname + '\'' +
                ", description='" + description + '\'' +
                ", distinguishedname='" + distinguishedname + '\'' +
                ", memberof='" + memberof + '\'' +
                ", name='" + name + '\'' +
                ", extensionAttribute1='" + extensionAttribute1 + '\'' +
                ", extensionAttribute2='" + extensionAttribute2 + '\'' +
                ", extensionAttribute10='" + extensionAttribute10 + '\'' +
                ", objectCategory='" + objectCategory + '\'' +
                ", objectclass='" + objectclass + '\'' +
                ", nome='" + nome + '\'' +
                ", login='" + login + '\'' +
                ", senha='" + senha + '\'' +
                ", email='" + email + '\'' +
                ", admin=" + admin +
                ", adminEmprestimo=" + adminEmprestimo +
                ", ativo=" + ativo +
                '}';
    }
}
