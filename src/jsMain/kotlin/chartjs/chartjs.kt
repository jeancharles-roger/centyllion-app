@file:Suppress("unused", "FunctionName", "PropertyName")
package chartjs

import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.MouseEvent

external interface ChartElement {
    val _chart: Chart
    val _datasetIndex: Number
    val _index: Number
}

external interface ChartAnimation {
    /** Chart object*/
    val chart: Chart

    /** Current Animation frame number */
    val currentStep: Number

    /** Number of animation frames */
    val numSteps: Number

    /** Animation easing to use */
    val easing: String

    /** Function that renders the chart */
    val render: (ChartAnimation) -> Unit

    /** User callback */
    val onAnimationProgress: (ChartAnimation) -> Unit

    /** User callback */
    val onAnimationComplete: (ChartAnimation) -> Unit
}

external interface TooltipItem {
    /** X Value of the tooltip as a string */
    var xLabel: Number

    /** Y value of the tooltip as a string */
    var yLabel: Number

    /** Index of the dataset the item comes from */
    var datasetIndex: Number

    /** Index of this data item in the dataset */
    var index: Number

    /** X position of matching point */
    var x: Number

    /** Y position of matching point */
    var y: Number
}

external interface TooltipModel {
    // The items that we are rendering in the tooltip. See Tooltip Item Interface section
    val dataPoints: Array<TooltipItem>?

    // Positioning
    var xPadding: Number
    var yPadding: Number
    var xAlign: String
    var yAlign: String

    // X and Y properties are the top left of the tooltip
    var x: Number
    var y: Number
    var width: Number
    var height: Number
    // Where the tooltip points to
    var caretX: Number
    var caretY: Number

    /** Body
    // The body lines that need to be rendered
    // Each object contains 3 parameters
    // before: Array<String> // lines of text before the line with the color square
    // lines: Array<String>, // lines of text to render as the main item with color square
    // after: Array<String>, // lines of text to render after the main lines
     */
    var body: Array<Any>
    /** lines of text that appear after the title but before the body */
    var beforeBody: Array<String>
    /** line of text that appear after the body and before the footer */
    var afterBody: Array<String>
    var bodyFontColor: String
    var _bodyFontFamily: String
    var _bodyFontStyle: String
    var _bodyAlign: String
    var bodyFontSize: Number
    var bodySpacing: Number

    // Title
    // lines of text that form the title
    var title: Array<String>
    var titleFontColor: String
    var _titleFontFamily: String
    var _titleFontStyle: String
    var titleFontSize: Number
    var _titleAlign: String
    var titleSpacing: Number
    var titleMarginBottom: Number

    // Footer
    // lines of text that form the footer
    var footer: Array<String>
    var footerFontColor: String
    var _footerFontFamily: String
    var _footerFontStyle: String
    var footerFontSize: Number
    var _footerAlign: String
    var footerSpacing: Number
    var footerMarginTop: Number

    // Appearance
    var caretSize: Number
    var cornerRadius: Number
    var backgroundColor: String

    // colors to render for each item in body[]. This is the color of the squares in the tooltip
    var labelColors: Array<String>

    // 0 opacity is a hidden tooltip
    var opacity: Number
    var legendColorBackground: String
    var displayColors: Boolean
}

@JsModule("chartjs")
external class Chart {

    constructor(context: CanvasRenderingContext2D, config: LineChartConfig = definedExternally)

    constructor(context: HTMLCanvasElement, config: LineChartConfig = definedExternally)


    /** Use this to destroy any chart instances that are created. This will clean up any references stored to the chart object within Chart.js, along with any associated event listeners attached by Chart.js. This must be called before the canvas is reused for a new chart.*/
    fun destroy()

    /**
     * Triggers an update of the chart. This can be safely called after updating the data object. This will update all scales, legends, and then re-render the chart.

    ```
    // duration is the time for the animation of the redraw in milliseconds
    // lazy is a boolean. if true, the animation can be interrupted by other animations
    myLineChart.data.datasets[0].data[2] = 50; // Would update the first dataset's value of 'March' to be 50
    myLineChart.update(); // Calling update now animates the position of March from 90 to 50.
    ```

    A config object can be provided with additional configuration for the update process.
    This is useful when update is manually called inside an event handler and some different animation is desired.

    See Updating Charts for more details.
     */
    fun update(config: ChartUpdateConfig? = definedExternally)

    /** Reset the chart to it's state before the initial animation. A new animation can then be triggered using update.*/
    fun reset()

    /** Triggers a redraw of all chart elements. Note, this does not update elements for new data. Use .update() in that case.*/
    fun render(config: ChartUpdateConfig? = definedExternally)

    /** Use this to stop any current animation loop. This will pause the chart during any current animation frame. Call .render() to re-animate.*/
    fun stop()

    /** Use this to manually resize the canvas element. This is run each time the canvas container is resized, but you can call this method manually if you change the size of the canvas nodes container element.*/
    fun resize()

    /** Will clear the chart canvas. Used extensively internally between animation frames, but you might find it useful. */
    fun clear()

    /** This returns a base 64 encoded string of the chart in it's current state. */
    fun toBase64Image(): String

    /** Returns an HTML string of a legend for that chart. The legend is generated from the legendCallback in the options. */
    fun generateLegend(): String

    /**
     * Calling getElementAtEvent(event) on your Chart instance passing an argument of an event, or jQuery event, will return the single element at the event position. If there are multiple items within range, only the first is returned. The value returned from this method is an array with a single parameter. An array is used to keep a consistent API between the get*AtEvent methods.
    ```
    myLineChart.getElementAtEvent(e);
    // => returns the first element at the event point.
    To get an item that was clicked on, getElementAtEvent can be used.

    function clickHandler(evt) {
    var firstPoint = myChart.getElementAtEvent(evt)[0];

    if (firstPoint) {
    var label = myChart.data.labels[firstPoint._index];
    var value = myChart.data.datasets[firstPoint._datasetIndex].data[firstPoint._index];
    }
    }
    ```
     */
    fun getElementAtEvent(e: MouseEvent): ChartElement?

    /**
     * Looks for the element under the event point, then returns all elements at the same data index. This is used internally for 'label' mode highlighting.
     * Calling getElementsAtEvent(event) on your Chart instance passing an argument of an event, or jQuery event, will return the point elements that are at that the same position of that event.
    ```
    canvas.onclick = function(evt){
    var activePoints = myLineChart.getElementsAtEvent(evt);
    // => activePoints is an array of points on the canvas that are at the same position as the click event.
    };
    ```
     * This functionality may be useful for implementing DOM based tooltips, or triggering custom behaviour in your application.
     */
    fun getElementsAtEvent(e: MouseEvent): ChartElement?

    /**
     * Looks for the element under the event point, then returns all elements from that dataset. This is used internally for 'dataset' mode highlighting
    ```
    myLineChart.getDatasetAtEvent(e);
    // => returns an array of elements
    ```
     */
    fun getDatasetAtEvent(e: MouseEvent): ChartElement?

    /**
     * Looks for the element for given X, it will return the element if any
     */
    fun getElementsAtXAxis(e: MouseEvent): Array<ChartElement>

    /**
     * Looks for the dataset that matches the current index and returns that metadata. This returned data has all of the metadata that is used to construct the chart.
     * The data property of the metadata will contain information about each point, rectangle, etc. depending on the chart type.
     *  Extensive examples of usage are available in the Chart.js tests.
    ```
    var meta = myChart.getDatasetMeta(0);
    var x = meta.data[0]._model.x
    ```
     */
    fun getDatasetMeta(index: Int): Any?

    var data: LineChartData = definedExternally
    var options: LineChartOptions = definedExternally

}
