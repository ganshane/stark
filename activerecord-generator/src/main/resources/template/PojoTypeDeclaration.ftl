/**
${pojo.getClassJavaDoc(pojo.getDeclarationName() + " generated by stark activerecord generator", 0)}
 */
object ${pojo.getDeclarationName()} extends ${pojo.importType("stark.activerecord.services.ActiveRecordInstance")}[${pojo.getDeclarationName()}]

<#include "Ejb3TypeDeclaration.ftl"/>
${pojo.getDeclarationType()} ${pojo.getDeclarationName()} extends ${pojo.importType("stark.activerecord.services.ActiveRecord")}