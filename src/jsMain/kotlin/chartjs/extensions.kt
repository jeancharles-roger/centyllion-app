@file:Suppress("unused", "FunctionName", "PropertyName")

package chartjs

import org.w3c.dom.events.MouseEvent

class AnimationOptions(
    /** The number of milliseconds an animation takes. */
    var duration: Number = 1000,
    /** Easing function to use. [Easing](http://www.chartjs.org/docs/latest/configuration/animations.html#easing) */
    var easing: String = "easeOutQuart",
    /** Callback called on each step of an animation. */
    var onProgress: ((ChartAnimation) -> Unit)? = null,
    /** Callback called at the end of an animation. */
    var onComplete: ((ChartAnimation) -> Unit)? = null
)

class LayoutOptions(
    /** The padding to add inside the chart. */
    var padding: Number = 0
)

class TooltipsOptions(
    /** Are on-canvas tooltips enabled */
    var enabled: Boolean = true,
    /** See custom tooltip section. */
    var custom: ((TooltipModel) -> Unit)? = null,
    /** Sets which elements appear in the tooltip. more.... */
    var mode: String = "nearest",
    /** if true, the tooltip mode applies only when the mouse position intersects with an element. If false, the mode will be applied at all times. */
    var intersect: Boolean = true,
    /** The mode for positioning the tooltip. more... */
    var position: String = "average",
    /** See the callbacks section */
    //var callbacks: Any? = undefined,

    /** Sort tooltip items. more... */
    //var itemSort:Function? = undefined,
    /** Filter tooltip items. more... */
    //var filter: Function? = undefined,

    /** Background color of the tooltip. */
    var backgroundColor: String = "rgba(0,0,0,0.8)",
    /** title font */
    var titleFontFamily: String = "'Helvetica Neue', 'Helvetica', 'Arial', sans-serif",
    /** Title font size */
    var titleFontSize: Number = 12,
    /** Title font style */
    var titleFontStyle: String = "bold",
    /** Title font color */
    var titleFontColor: String = "#fff",
    /** Spacing to add to top and bottom of each title line. */
    var titleSpacing: Number = 2,
    /** Margin to add on bottom of title section. */
    var titleMarginBottom: Number = 6,
    /** body line font */
    var bodyFontFamily: String = "'Helvetica Neue', 'Helvetica', 'Arial', sans-serif",
    /** Body font size */
    var bodyFontSize: Number = 12,
    /** Body font style */
    var bodyFontStyle: String = "normal",
    /** Body font color */
    var bodyFontColor: String = "#fff",
    /** Spacing to add to top and bottom of each tooltip item. */
    var bodySpacing: Number = 2,
    /** footer font */
    var footerFontFamily: String = "'Helvetica Neue', 'Helvetica', 'Arial', sans-serif",
    /** Footer font size */
    var footerFontSize: Number = 12,
    /** Footer font style */
    var footerFontStyle: String = "bold",
    /** Footer font color */
    var footerFontColor: String = "#fff",
    /** Spacing to add to top and bottom of each footer line. */
    var footerSpacing: Number = 2,
    /** Margin to add before drawing the footer. */
    var footerMarginTop: Number = 6,
    /** Padding to add on left and right of tooltip. */
    var xPadding: Number = 6,
    /** Padding to add on top and bottom of tooltip. */
    var yPadding: Number = 6,
    /** Extra distance to move the end of the tooltip arrow away from the tooltip point. */
    var caretPadding: Number = 2,
    /** Size, in px, of the tooltip arrow. */
    var caretSize: Number = 5,
    /** Radius of tooltip corner curves. */
    var cornerRadius: Number = 6,
    /** Color to draw behind the colored boxes when multiple items are in the tooltip */
    var multiKeyBackground: String = "#fff",
    /** if true, color boxes are shown in the tooltip */
    var displayColors: Boolean = true,
    /** Color of the border */
    var borderColor: String = "rgba(0,0,0,0)",
    /** Size of the border */
    var borderWidth: Number = 0
)

class LinearTickOptions(
    //  TickOptions

    /** If true, automatically calculates how many labels that can be shown and hides labels accordingly. Turn it off to show all labels no matter what */
    var autoSkip: Boolean = true,
    /** Padding between the ticks on the horizontal axis when autoSkip is enabled. Note: Only applicable to horizontal scales. */
    var autoSkipPadding: Number = 0,
    /** Distance in pixels to offset the label from the centre point of the tick (in the y direction for the x axis, and the x direction for the y axis). Note: this can cause labels at the edges to be cropped by the edge of the canvas */
    var labelOffset: Number = 0,
    /** Maximum rotation for tick labels when rotating to condense labels. Note: Rotation doesn't occur until necessary. Note: Only applicable to horizontal scales. */
    var maxRotation: Number = 90,
    /** Minimum rotation for tick labels. Note: Only applicable to horizontal scales. */
    var minRotation: Number = 0,
    /** Flips tick labels around axis, displaying the labels inside the chart instead of outside. Note: Only applicable to vertical scales. */
    var mirror: Boolean = false,
    /** Padding between the tick label and the axis. When set on a vertical axis, this applies in the horizontal (X) direction. When set on a horizontal axis, this applies in the vertical (Y) direction. */
    var padding: Number = 10,

    // Specific

    /** if true, scale will include 0 if it is not already included. */
    var beginAtZero: Boolean? = undefined,
    /** User defined minimum number for the scale, overrides minimum value from data. */
    //var min: Number = undefined,
    /** User defined maximum number for the scale, overrides maximum value from data. */
    //var max: Number = undefined,

    /** Maximum number of ticks and gridlines to show. */
    var maxTicksLimit: Number = 11,
    /** User defined fixed step size for the scale. */
    var stepSize: Number? = undefined,
    /** Adjustment used when calculating the maximum data value. */
    var suggestedMax: Number? = undefined,
    /** Adjustment used when calculating the minimum data value. */
    var suggestedMin: Number? = undefined
)

class GridLinesOptions(
    /** If false, do not display grid lines for this axis. */
    var display: Boolean = true,
    /** The color of the grid lines. If specified as an array, the first color applies to the first grid line, the second to the second grid line and so on. */
    var color: String = "rgba(0, 0, 0, 0.1)",
    /** Length and spacing of dashes on grid lines.*/
    var borderDash: Array<Number> = emptyArray(),
    /** Offset for line dashes. */
    var borderDashOffset: Number = 0,
    /** Stroke width of grid lines. */
    var lineWidth: Number = 1,
    /** If true, draw border at the edge between the axis and the chart area. */
    var drawBorder: Boolean = true,
    /** If true, draw lines on the chart area inside the axis lines. This is useful when there are multiple axes and you need to control which grid lines are drawn. */
    var drawOnChartArea: Boolean = true,
    /** If true, draw lines beside the ticks in the axis area beside the chart. */
    var drawTicks: Boolean = true,
    /** Length in pixels that the grid lines will draw into the axis area. */
    var tickMarkLength: Number = 10,
    /** Stroke width of the grid line for the first index (index 0). */
    var zeroLineWidth: Number = 1,
    /** Stroke color of the grid line for the first index (index 0). */
    var zeroLineColor: String = "rgba(0, 0, 0, 0.25)",
    /** Length and spacing of dashes of the grid line for the first index (index 0). */
    var zeroLineBorderDash: Array<Number> = emptyArray(),
    /** Offset for line dashes of the grid line for the first index (index 0). */
    var zeroLineBorderDashOffset: Number = 0,
    /** If true, grid lines will be shifted to be between labels. This is set to true in the bar chart by default. */
    var offsetGridLines: Boolean = false
)

class ScaleLabelOptions(
    /** If true, display the axis title. */
    var display: Boolean = false,
    /** The text for the title. (i.e. "# of People" or "Response Choices"). */
    var labelString: String = "",
    /** Height of an individual line of text (see MDN) */
    var lineHeight: Number = 1.2,
    /** Font color for scale title. */
    var fontColor: String = "#666",
    /** Font family for the scale title, follows CSS font-family options. */
    var fontFamily: String = "'Helvetica Neue', 'Helvetica', 'Arial', sans-serif",
    /** Font size for scale title. */
    var fontSize: Number = 12,
    /** Font style for the scale title, follows CSS font-style options (i.e. normal, italic, oblique, initial, inherit). */
    var fontStyle: String = "normal",
    /** Padding to apply around scale labels. Only top and bottom are implemented. */
    var padding: Number = 4
)

class LinearAxisOptions(
    /** Position of the axis in the chart. Possible values are: 'top', 'left', 'bottom', 'right' */
    var position: String = "bottom",
    /** If true, extra space is added to the both edges and the axis is scaled to fit into the chart area. This is set to true in the bar chart by default. */
    var offset: Boolean = false,
    /** The ID is used to link datasets and scale axes together. more... */
    var id: String? = undefined,
    /** Grid line configuration. */
    var gridLines: GridLinesOptions = GridLinesOptions(),
    /** Scale title configuration. */
    var scaleLabel: ScaleLabelOptions = ScaleLabelOptions(),
    /* Tick configuration. */
    var ticks: LinearTickOptions = LinearTickOptions()
) {
    val type = "linear"
}

class ScalesOptions(
    var xAxes: Array<LinearAxisOptions> = emptyArray(),
    var yAxes: Array<LinearAxisOptions> = emptyArray()
)

class LineChartOptions(
    /** Resizes the chart canvas when its container does (important note...). */
    var responsive: Boolean = true,
    /** Duration in milliseconds it takes to animate to new size after a resize event. */
    var responsiveAnimationDuration: Number = 0,
    /** Maintain the original canvas aspect ratio (width / height) when resizing. */
    var maintainAspectRatio: Boolean = true,
    /** Called when a resize occurs. Gets passed two arguments: the chart instance and the new size. */
    var onResize: ((Chart, Any) -> Unit)? = null,

    var animation: AnimationOptions = AnimationOptions(),
    var layout: LayoutOptions = LayoutOptions(),
    var scales: ScalesOptions = ScalesOptions(),
    // TODO adds legend options
    // TODO adds title options


    var dragData: Boolean = false,
    var dragX: Boolean = false,

    var onDragStart: ((event: Any, element: ChartElement) -> Boolean)? = null,
    var onDrag: ((event: Any, datasetIndex: Number, index: Number, value: LineChartPlot) -> Unit)? = null,
    var onDragEnd: ((event: Any, datasetIndex: Number, index: Number, value: LineChartPlot) -> Unit)? = null,

    var tooltips: TooltipsOptions = TooltipsOptions(),
    //var hover: Any? = undefined
    /** The events option defines the browser events that the chart should listen to for tooltips and hovering. more... */
    var events: Array<String> = arrayOf("mousemove", "mouseout", "click", "touchstart", "touchmove", "touchend"),
    /** Called when any of the events fire. Called in the context of the chart and passed the event and an array of active elements (bars, points, etc). */
    var onHover: ((Any, Array<Any>) -> Unit)? = null,
    /** Called if the event is of type 'mouseup' or 'click'. Called in the context of the chart and passed the event and an array of active elements */
    var onClick: ((event: MouseEvent, elements: Array<Any>) -> Unit)? = null

)

abstract class ChartDataSet

class LineChartPlot(
    var x: Number,
    var y: Number
)

class LineDataSet(
    var key: Int? = undefined,

    /** The label for the dataset which appears in the legend and tooltips. */
    var label: String? = undefined,
    /** Data for the set */
    var data: Array<LineChartPlot>? = undefined,
    /** The ID of the x axis to plot this dataset on. If not specified, this defaults to the ID of the first found x axis */
    var xAxisID: String? = undefined,
    /** The ID of the y axis to plot this dataset on. If not specified, this defaults to the ID of the first found y axis. */
    var yAxisID: String? = undefined,
    /** The fill color under the line. See [Colors](http://www.chartjs.org/docs/latest/general/colors.html) */
    var backgroundColor: String? = undefined,
    /** The color of the line. See [Colors](http://www.chartjs.org/docs/latest/general/colors.html) */
    var borderColor: String? = undefined,
    /** The width of the line in pixels. */
    var borderWidth: Number? = undefined,
    /** Length and spacing of dashes. See [MDN](https://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D/setLineDash) */
    var borderDash: Array<Number>? = undefined,
    /** Offset for line dashes. See [MDN](https://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D/lineDashOffset) */
    var borderDashOffset: Number? = undefined,
    /** Cap style of the line. See MDN */
    var borderCapStyle: String? = undefined,
    /** Line joint style. See MDN */
    var borderJoinStyle: String? = undefined,
    /** Algorithm used to interpolate a smooth curve from the discrete data points. more... */
    var cubicInterpolationMode: String? = undefined,
    /** How to fill the area under the line.
     * - Absolute dataset index:	1, 2, 3, ...
     * - Relative dataset index:	'-1', '-2', '+1', ...
     * - Boundary:	'start', 'end', 'origin'
     * - Disabled:	false
     */
    var fill: String? = undefined,
    /** Bezier curve tension of the line. Set to 0 to draw straightlines. This option is ignored if monotone cubic interpolation is used. */
    var lineTension: Number? = undefined,
    /** The fill color for points. */
    var pointBackgroundColor: String? = undefined,
    /** The border color for points. */
    var pointBorderColor: String? = undefined,
    /** The width of the point border in pixels. */
    var pointBorderWidth: Number? = undefined,
    /** The radius of the point shape. If set to 0, the point is not rendered. */
    var pointRadius: Number? = undefined,
    /** Style of the point. */
    var pointStyle: String? = undefined,
    /** The pixel size of the non-displayed point that reacts to mouse events. */
    var pointHitRadius: Number? = undefined,
    /** Point background color when hovered. */
    var pointHoverBackgroundColor: String? = undefined,
    /** Point border color when hovered. */
    var pointHoverBorderColor: String? = undefined,
    /** Border width of point when hovered. */
    var pointHoverBorderWidt: Number? = undefined,
    /** The radius of the point when hovered. */
    var pointHoverRadius: Number? = undefined,
    /** If false, the line is not drawn for this dataset. */
    var showLine: Boolean? = undefined,
    /** If true, lines will be drawn between points with no or null data. If false, points with NaN data will create a break in the line */
    var spanGaps: Boolean? = undefined,
    /** If the line is shown as a stepped line. */
    var steppedLine: Boolean? = undefined,
    /** Avoid drag for this data set*/
    var dragData: Boolean? = undefined
) : ChartDataSet()


class LineChartData(
    var datasets: Array<LineDataSet> = emptyArray(),
    var labels: Array<Number> = emptyArray()
)

class LineChartConfig(
    var data: LineChartData = LineChartData(),
    var options: LineChartOptions = LineChartOptions()
) {
    val type = "line"
}

class ChartUpdateConfig(
    /** Time for the animation of the redraw in milliseconds */
    val duration: Number? = undefined,
    /** If true, the animation can be interrupted by other animations */
    val lazy: Boolean? = undefined,
    /** The animation easing function. See Animation Easing for possible values. */
    val easing: String? = undefined
)
