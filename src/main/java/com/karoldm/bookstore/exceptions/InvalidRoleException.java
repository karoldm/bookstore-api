package com.karoldm.bookstore.exceptions;

import com.karoldm.bookstore.enums.Roles;

public class InvalidRoleException extends RuntimeException {
    public InvalidRoleException(Roles role){
        super("Usuário com role " + role.name() + " não tem acesso a esse recurso.");
    }
}
