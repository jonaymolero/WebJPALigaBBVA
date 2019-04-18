<%-- 
    Document   : apuestas
    Created on : 05-feb-2019, 18:59:50
    Author     : Usuario
--%>

<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    List listaApuestas= (List) request.getAttribute("infoapuestas");
    String nombrePartido = (String) request.getAttribute("nombrepartido");
    if(listaApuestas.size()==0){
%>
    <h5> <%=nombrePartido%> </h5>
    <h6>Este partido no tiene apuestas</h6>
<%}else{%>  
    <h5> <%=nombrePartido%> </h5>
    <table>
        <thead>
          <tr>
              <th>Apuesta</th>
              <th>Nº apuestas totales</th>
          </tr>
        </thead>
        <tbody>
          <%
              String informacion=null;
              for(int i=0;i<listaApuestas.size();i++){
              informacion=(String) listaApuestas.get(i);
          %>
            <tr>
                <td><%=informacion.substring(0,informacion.indexOf(","))%></td>
                <td><%=informacion.substring(informacion.indexOf(",")+1)%></td>
            </tr>
          <%}%>
        </tbody>
    </table>
<%}%>