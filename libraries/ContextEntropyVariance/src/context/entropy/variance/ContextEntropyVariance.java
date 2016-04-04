/**
 * "ContextEntropyVariance" Java class is free software: you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version always keeping
 * the additional terms specified in this license.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 *
 * Additional Terms of this License
 * --------------------------------
 *
 * 1. It is Required the preservation of specified reasonable legal notices
 *   and author attributions in that material and in the Appropriate Legal
 *   Notices displayed by works containing it.
 *
 * 2. It is limited the use for publicity purposes of names of licensors or
 *   authors of the material.
 *
 * 3. It is Required indemnification of licensors and authors of that material
 *   by anyone who conveys the material (or modified versions of it) with
 *   contractual assumptions of liability to the recipient, for any liability
 *   that these contractual assumptions directly impose on those licensors
 *   and authors.
 *
 * 4. It is Prohibited misrepresentation of the origin of that material, and it is
 *   required that modified versions of such material be marked in reasonable
 *   ways as different from the original version.
 *
 * 5. It is Declined to grant rights under trademark law for use of some trade
 *   names, trademarks, or service marks.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (lgpl.txt).  If not, see <http://www.gnu.org/licenses/>
 */

package context.entropy.variance;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * This class represents the variance which exists in the context entropy.
 *  
 * @author UPM (member of RECLAMO Development Team)(http://reclamo.inf.um.es)
 * @version 1.0
 */
public class ContextEntropyVariance {
    private double totalCEV;
    private double partialCEV;
    private ContextAnomalyIndicatorList t1AnomalyList;
    private ContextAnomalyIndicatorList t2AnomalyList;
    private IndicatorRenyiCrossEntropyList t1RCEList;
    private IndicatorRenyiCrossEntropyList t2RCEList;
    private IndicatorRenyiCrossEntropyList partial_RCE_list;
    private static final int NUMEROITEMS = 11;
    private static final double ENTROPYMAX = 2.0827853703;

    private static Logger _log = Logger.getLogger(ContextEntropyVariance.class.getName());

    public ContextEntropyVariance (){
    }

    public double getTotalContextEntropyVariance(ContextAnomalyIndicatorList
            intrusionAnomalyList, ContextAnomalyIndicatorList responseAnomalyList){
        //Inicializamos listas;
        t1AnomalyList = intrusionAnomalyList;
        t2AnomalyList = responseAnomalyList;

        //Comprobamos que ambas tienen la misma longitud

        if((t1AnomalyList.getContextAnomalyIndicatorList().size())==(t2AnomalyList.getContextAnomalyIndicatorList().size())){
            /*Para cada componente de cada lista obtenemos la entropía cruzada
             * de renyi respecto a su valor en T0.
             * Como resultado se obtiene una lista de entropias cruzada de Renyi
             * asociada a cada lista.
             */
            t1RCEList = getRCEList(t1AnomalyList);
            t2RCEList = getRCEList(t2AnomalyList);

            /* Para cada valor de entropía calculamos la variacion entre las
             * listas y obtenemos la varianza total
             */
            totalCEV = getContextRCEVariance (t1RCEList, t2RCEList);
            return totalCEV;
        }
        else {
            _log.warning("Las listas no tienen la misma longitud por lo que no " +
                    "es posible calcular la variación de la entropia");
            return 4.0;
        }

    }
    
    public double getPartialContextEntropyVariance(ContextAnomalyIndicatorList list){
        ContextAnomalyIndicatorList partial_list = list;
        /* Para cada componente de la lista obtenemos la entropía cruzada
         * de renyi respecto a su valor en T0.
         * Como resultado se obtiene una lista de entropias cruzada de Renyi.
         */
        partial_RCE_list = getRCEList(partial_list);
        /* Para cada valor de entropía calculamos la variacion entre la lista
         * y el valor en estado inicial teniendo en cuenta los pesos.
         */
        partialCEV = getContextRCEPartialVariance (partial_RCE_list);
        return partialCEV;
        
    }
    
    /*Método que obtiene la lista de entropias cruzadas de renyi para cada
     * indicador proporcionado en una lista.
     */
    private IndicatorRenyiCrossEntropyList getRCEList(ContextAnomalyIndicatorList list){
        ContextAnomalyIndicator indicator;
        ContextAnomalyIndicatorList indicatorList = list;
        IndicatorRenyiCrossEntropyList entropyList = new IndicatorRenyiCrossEntropyList();
        Iterator i = indicatorList.getContextAnomalyIndicatorList().iterator();
        while(i.hasNext()){
            IndicatorRenyiCrossEntropy entropy;
            indicator = (ContextAnomalyIndicator) i.next();
            int indicatorValue = indicator.getIndicatorValue();
            //Para cada indicador de la lista calculo su RCE
            double rceValue = getRCEValue(indicatorValue);
            //_log.info("El valor de la RCE del indicador "+indicator.getIndicatorName()+" es: "+rceValue);
            entropy = new IndicatorRenyiCrossEntropy (indicator.getIndicatorName(),rceValue,indicator.getIndicatorWeight());
            entropyList.addContextAnomalyIndicator(entropy);
        }
        return entropyList;
    }
  
    /* Metodo que calcula el valor de la entropia cruzada de renyi */
    private double getRCEValue(int indicator){
        int anindiValue = indicator;
        double rceValue = 2*(Math.log10(((NUMEROITEMS - anindiValue)* Math.sqrt (1))));
        //_log.info("rceValue = "+rceValue);
        double rceNorm = Math.pow((rceValue/ENTROPYMAX),2);
        return rceNorm;
    }
    
    private double getContextRCEVariance(IndicatorRenyiCrossEntropyList list1, IndicatorRenyiCrossEntropyList list2){
        IndicatorRenyiCrossEntropyList entropyList1 = list1;
        IndicatorRenyiCrossEntropyList entropyList2 = list2;
        IndicatorRenyiCrossEntropy entropy2;
        IndicatorRenyiCrossEntropy entropy1;
   
        Iterator itList2 = entropyList2.getIndicatorRenyiCrossEntropyList().iterator();
        double rceVariance = 0.0;
        int numIndicadores = 0;
        while(itList2.hasNext()){
            entropy2 = (IndicatorRenyiCrossEntropy) itList2.next();
            String indiName2 = entropy2.getIndicatorName();
            Iterator itList1 = entropyList1.getIndicatorRenyiCrossEntropyList().iterator();
            while (itList1.hasNext()){
                entropy1 = (IndicatorRenyiCrossEntropy) itList1.next();
                String indiName1 = entropy1.getIndicatorName();
                if(indiName1.equalsIgnoreCase(indiName2)){
                    numIndicadores++;
                    if(entropy2.getIndicatorWeight() != entropy1.getIndicatorWeight()){
                        _log.warning("Incompatibilidad en pesos. Revisar valores");
                    }
                    else{
                        //_log.info("Actualizando el valor de RCE por "+numIndicadores+ " vez.");

                        //_log.info("el nombre del indicador es: "+entropy2.getIndicatorName()+" ; y su peso es: "+entropy2.getIndicatorWeight());
                        double rcePartial = (entropy2.getEntropyValue() - entropy1.getEntropyValue())*(entropy2.getIndicatorWeight());
                        rceVariance = rceVariance + rcePartial;
                    }
                }

            }
        }
        return rceVariance;
    }
    
    private double getContextRCEPartialVariance(IndicatorRenyiCrossEntropyList list1){
        IndicatorRenyiCrossEntropyList entropyList1 = list1;
        IndicatorRenyiCrossEntropy entropy1;
        double rceVariance = 0.0;
        Iterator itList1 = entropyList1.getIndicatorRenyiCrossEntropyList().iterator();
        while (itList1.hasNext()){
            entropy1 = (IndicatorRenyiCrossEntropy) itList1.next();
            String indiName1 = entropy1.getIndicatorName();
            //_log.info("Actualizando el valor de RCE por "+numIndicadores+ " vez.");
            //_log.info("el nombre del indicador es: "+indiName1+" ; y su peso es: "+entropy1.getIndicatorWeight());
            double rcePartial = (entropy1.getEntropyValue()*entropy1.getIndicatorWeight());
            rceVariance = rceVariance + rcePartial;
        }

        return rceVariance;
    }
}