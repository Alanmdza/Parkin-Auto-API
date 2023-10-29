import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class Lugar {
    private String lugar;
    private String patente;
    private boolean Ocupado;
    public boolean getOcupado(){
        return Ocupado;
    }
}
