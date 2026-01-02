package com.github.dirify21.aml.api;

public interface IIconRegister {
    IIcon registerIcon(String name);

    default IIcon func_94245_a(String name) {
        return registerIcon(name);
    }
}