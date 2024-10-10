package mg.itu.prom16;

import mg.itu.prom16.AnnotationController;
import mg.itu.prom16.GetAnnotation;
import mg.itu.prom16.Post;
import mg.itu.prom16.Param;
import mg.itu.prom16.CustomSession;
import mg.itu.prom16.Restapi;
import mg.itu.prom16.VerbMethode;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;

import mg.itu.prom16.ModelView; 
import mg.itu.prom16.RequestBody;
import java.lang.reflect.Field;  
import java.io.*;
import java.lang.reflect.Executable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import jakarta.servlet.RequestDispatcher; 
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.google.gson.Gson;

public class FrontController extends HttpServlet {
    private List<String> controller = new ArrayList<>();
    private String controllerPackage;
    boolean checked = false;
    HashMap<String, Mapping> lien = new HashMap<>();
    String error = "";

    @Override
    public void init() throws ServletException {
        super.init();
        controllerPackage = getInitParameter("controller-package");
        try {
            this.scan();
        } catch (Exception e) {
            error = e.getMessage();
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");

        String[] requestUrlSplitted = request.getRequestURL().toString().split("/");
        String controllerSearched = requestUrlSplitted[requestUrlSplitted.length - 1];
        String verb = request.getMethod();  // GET ou POST

        if (!error.isEmpty()) {
            out.println(error);
            out.close();
            return;
        }

        if (!lien.containsKey(controllerSearched)) {
            out.println("<p>Méthode non trouvée pour cette URL.</p>");
            out.close();
            return;
        }

        Mapping mapping = lien.get(controllerSearched);

        // Vérifiez si c'est un GET ou POST et trouvez la méthode correspondante
        Method method = null;
        try {
            Class<?> clazz = Class.forName(mapping.getClassName());
            for (Method m : clazz.getDeclaredMethods()) {
                if (verb.equalsIgnoreCase("GET") && m.isAnnotationPresent(GetAnnotation.class)) {
                    GetAnnotation getAnnotation = m.getAnnotation(GetAnnotation.class);
                    if (getAnnotation.value().equals(controllerSearched)) {
                        method = m;
                        break;
                    }
                } else if (verb.equalsIgnoreCase("POST") && m.isAnnotationPresent(Post.class)) {
                    Post postAnnotation = m.getAnnotation(Post.class);
                    if (postAnnotation.value().equals(controllerSearched)) {
                        method = m;
                        break;
                    }
                }
            }

            if (method == null) {
                out.println("<p>Aucune méthode correspondante trouvée pour cette URL et ce verbe HTTP.</p>");
                out.close();
                return;
            }

            Object controllerInstance = clazz.getDeclaredConstructor().newInstance();
            Object returnValue = method.invoke(controllerInstance);

            if (returnValue instanceof String) {
                out.println("Résultat: " + returnValue);
            } else {
                out.println("<p>Type de retour non reconnu.</p>");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("<p>Erreur: " + e.getMessage() + "</p>");
        }

        out.close();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    public void scan() throws Exception {
        String classesPath = getServletContext().getRealPath("/WEB-INF/classes");
        String decodedPath = URLDecoder.decode(classesPath, "UTF-8");
        String packagePath = decodedPath + "\\" + controllerPackage.replace('.', '\\');
        File packageDirectory = new File(packagePath);
    
        if (!packageDirectory.exists() || !packageDirectory.isDirectory()) {
            throw new Exception("Package n'existe pas");
        }
    
        File[] classFiles = packageDirectory.listFiles((dir, name) -> name.endsWith(".class"));
        if (classFiles != null) {
            for (File classFile : classFiles) {
                String className = controllerPackage + '.' + classFile.getName().substring(0, classFile.getName().length() - 6);
                Class<?> clazz = Class.forName(className);
    
                if (clazz.isAnnotationPresent(AnnotationController.class)) {
                    Method[] methods = clazz.getDeclaredMethods();
    
                    for (Method method : methods) {
                        if (method.isAnnotationPresent(GetAnnotation.class)) {
                            GetAnnotation getAnnotation = method.getAnnotation(GetAnnotation.class);
                            if (getAnnotation.value().equals(controllerSearched)) {
                                method = m;
                                break;
                            }
                        } else if (method.isAnnotationPresent(Post.class)) {
                            Post postAnnotation = method.getAnnotation(Post.class);
                            if (postAnnotation.value().equals(controllerSearched)) {
                                method = m;
                                break;
                            }
                        }                        
                    }
                }
            }
        }
    }       

    private Object[] getMethodParameters(Method method, HttpServletRequest request, HttpServletResponse response) {
        Parameter[] parameters = method.getParameters();
        Object[] params = new Object[parameters.length];
    
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
    
            // Vérifiez si le paramètre est annoté avec @Param
            if (parameter.isAnnotationPresent(Param.class)) {
                String paramName = parameter.getAnnotation(Param.class).value();
                String paramValue = request.getParameter(paramName);
                // Convertir la valeur en type approprié
                params[i] = convertParameter(paramValue, parameter.getType());
            } else if (parameter.getType() == HttpServletRequest.class) {
                // Si le paramètre est de type HttpServletRequest, passez-le directement
                params[i] = request;
            } else if (parameter.getType() == HttpServletResponse.class) {
                // Si le paramètre est de type HttpServletResponse, passez-le directement
                params[i] = response;  // Ici, cela fonctionnera maintenant
            } else {
                // Autres types que vous devez gérer
                params[i] = null; // Vous pouvez gérer cela selon vos besoins
            }
        }
    
        return params;
    }    
    
    private Object convertParameter(String value, Class<?> type) {
        if (value == null) {
            return null;
        }
        if (type == String.class) {
            return value;
        } else if (type == int.class || type == Integer.class) {
            return Integer.valueOf(value);
        } else if (type == double.class || type == Double.class) {
            return Double.valueOf(value);
        }
        // Ajoutez d'autres types selon vos besoins
        return null; // Retournez null ou gérez les types non pris en charge
    }    
}

class Mapping {
    String className;
    String methodeName;
    List<VerbMethode> verbMeth;  

    public Mapping(String className, String methodeName, List<VerbMethode> verbMeth) {
        this.className = className;
        this.methodeName = methodeName;
        this.verbMeth = verbMeth; 
    }

    public String getClassName() {
        return className;
    }

    public String getMethodeName() {
        return methodeName;
    }

    public List<VerbMethode> getVerbMeth() {
        return verbMeth;
    }

    public void setVerbMeth(List<VerbMethode> verbMeth) {
        this.verbMeth = verbMeth;
    }
}