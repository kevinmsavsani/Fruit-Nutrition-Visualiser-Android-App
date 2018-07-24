package kps.arnutrition.information;

/**
 * the class nutritional value specifies typical values regarding the nutritional value of a food
 * all weight information is in grams [g] and the caloric value information is in kilocalories
 * [kcal]
 */
public class NutritionalValue {
    // attribute section
    private float Calorie;     // specifies fat content in [g] per 100g of the food
    private float Protien;   // specifies protein content in [g] per 100g of the food
    private float Carbohydrate;      // specifies carb content in [g] per 100g of the food
    private float Fat;       // specifies fat content in [g] per 100g of the food
    private float MeanWeight;    // specifies the average weight of one piece or serving size



    /**
     * empty standard constructor
     *
     */
    public NutritionalValue(){}


    /**
     * standard constructor with transfer variables for all attributes
     *
     * @param Calorie       specifies fat content in [g] per 100g of the food
     * @param Protien     specifies protein content in [g] per 100g of the food
     * @param Carbohydrate        specifies carb content in [g] per 100g of the food
     * @param Fat         specifies fat content in [g] per 100g of the food
     * @param MeanWeight      specifies the average weight of one piece or serving size
     *                                  (medium size)
     */
    public NutritionalValue(float Calorie, float Protien,
                            float Carbohydrate, float Fat,
                            float MeanWeight) {
        this.Calorie = Calorie;
        this.Protien = Protien;
        this.Carbohydrate = Carbohydrate;
        this.Fat = Fat;
        this.MeanWeight = MeanWeight;
    }


    public float getCalorie() {
        return Calorie;
    }

    public void setCalorie(float Calorie) {
        this.Calorie = Calorie;
    }


    public float getProtien() {
        return Protien;
    }

    public void setProtien(float Protien) {
        this.Protien = Protien;
    }


    public float getCarbohydrate() {
        return Carbohydrate;
    }

    public void setCarbohydrate(float Carbohydrate) {
        this.Carbohydrate = Carbohydrate;
    }


    public float getFat() {
        return Fat;
    }

    public void setFat(float Fat) {
        this.Fat = Fat;
    }


    public float getMeanWeight() {
        return MeanWeight;
    }

    public void setMeanWeight(float MeanWeight) {
        this.MeanWeight = MeanWeight;
    }
}
