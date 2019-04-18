/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import entities.Jornada;
import entities.Partido;
import entities.Porra;
import entities.PorraPK;
import entities.Usuario;
import java.io.IOException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import jpautil.JPAUtil;

/**
 *
 * @author Gonzalo
 */
@WebServlet(name = "Controller", urlPatterns = {"/Controller"})
public class Controller extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        RequestDispatcher dispatcher;
        
        String op;
        String sql;
        Query query;
        EntityManager em = null;
        Usuario user;
        String msg;
        
        if (em == null) {
            em = JPAUtil.getEntityManagerFactory().createEntityManager();
            session.setAttribute("em", em);
        }
        
        op = request.getParameter("op");
        
        if (op.equals("inicio")) {
            sql = "select j from Jornada j";
            query = em.createQuery(sql);
            List<Jornada> jornadas = query.getResultList();           
            session.setAttribute("jornadas",jornadas);
            
            dispatcher = request.getRequestDispatcher("home.jsp");
            dispatcher.forward(request, response);
            
        }else if(op.equals("jornada")){
            sql = "select p from Partido p where p.idjornada.idjornada = :idJornada";

            Short idjornadillu;
            String idjornada = (String) request.getParameter("idJornada");
            if(idjornada.equals("")){
            idjornadillu = 0;
            }else{
            idjornadillu = Short.valueOf(idjornada);
            }
            query = em.createQuery(sql);
            query.setParameter("idJornada",idjornadillu );
            List <Partido> partidos = query.getResultList();                       
            session.setAttribute("partidos",partidos);
            session.setAttribute("idJornada",idjornada);
            
            dispatcher = request.getRequestDispatcher("home.jsp");
            dispatcher.forward(request, response);
            
        } else if (op.equals("login")) {
            String nombre = (String) request.getParameter("nombre");
            String dni = (String) request.getParameter("dni"); 
            user = em.find(Usuario.class, dni);
            
            if (user == null){
                Usuario nuevoUsuario = new Usuario(dni);
                nuevoUsuario.setNombre(nombre);
                em.getTransaction().begin();
                em.persist(nuevoUsuario);
                em.getTransaction().commit();
                user = nuevoUsuario;
                
                msg = "Usuario creado correctamente";
                request.setAttribute("msg", msg);
                session.setAttribute("usuario", user);
                
            } else if (user.getNombre().equals(nombre)){
                msg = "Bienvenido "+user.getNombre();
                request.setAttribute("msg", msg);
                session.setAttribute("usuario", user);
                
            } else {
                msg = "Introduce un nombre de usuario válido";
                request.setAttribute("msg", msg);
            }
            
            dispatcher = request.getRequestDispatcher("home.jsp");
            dispatcher.forward(request, response);
            
        } else if (op.equals("logout")){
            session.setAttribute("usuario", null);
            dispatcher = request.getRequestDispatcher("home.jsp");
            dispatcher.forward(request, response);
            
        } else if (op.equals("apostar")){  
            user = (Usuario) session.getAttribute("usuario");
            user = em.find(Usuario.class, user.getDni());
            
            int idpartido = Integer.valueOf(request.getParameter("idPartido")); 
            
            sql="select p from Porra p where p.usuario.dni=:dni and p.partido.idpartido=:idpartido";
            query = em.createQuery(sql);
            query.setParameter("dni", user.getDni());
            query.setParameter("idpartido", idpartido);
            List<String> lista=query.getResultList();
            
            if(lista.isEmpty()){
                Partido partido = em.find(Partido.class, idpartido);
                short goleslocal = Short.valueOf(request.getParameter("gLocal"));
                short golesvisitante = Short.valueOf(request.getParameter("gVisitante"));

                PorraPK porraPK = new PorraPK(user.getDni(), idpartido);

                Porra porra = new Porra(porraPK);
                porra.setUsuario(user);
                porra.setPartido(partido);
                porra.setGoleslocal(goleslocal);
                porra.setGolesvisitante(golesvisitante);

                em.getTransaction().begin();
                em.persist(porra);
                em.getTransaction().commit();
                
                msg = "Apuesta realizada";
                request.setAttribute("msg", msg);
            }else{
                msg = "Solo puedes apostar una vez en cada partido";
                request.setAttribute("msg", msg);
            }
            
            dispatcher = request.getRequestDispatcher("home.jsp");
            dispatcher.forward(request, response);
            
        } else if (op.equals("infoapuestas")){
            int idpartido = Integer.valueOf(request.getParameter("idpartido"));
            
            sql = "select concat(p.goleslocal,' - ',p.golesvisitante,',',count(p)) from Porra p where p.partido.idpartido = :idpartido group by p.goleslocal,p.golesvisitante"; 
            query = em.createQuery(sql);
            query.setParameter("idpartido", idpartido);
            List<String> lista=query.getResultList();
            
            Partido partido = em.find(Partido.class, idpartido);
            String nombrepartido = partido.getLocal().getNombre() + " - " + partido.getVisitante().getNombre();
            
            request.setAttribute("infoapuestas", lista);
            request.setAttribute("nombrepartido", nombrepartido);
            
            dispatcher = request.getRequestDispatcher("apuestas.jsp");
            dispatcher.forward(request, response);
        } 
        
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
