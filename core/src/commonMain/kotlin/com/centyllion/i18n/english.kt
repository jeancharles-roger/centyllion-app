package com.centyllion.i18n

object English: Locale {
    override val name: String = "en-US"

    override val label: String = "English"

    override fun value(key: String): String = when (key) {
        "Grain with id %0 doesn't exist" -> "Agent with id %0 doesn't exist"
        "Behaviour must have a main reactive" -> "Behaviour must have a main reactive"
        "Speed must be between 0 and 1" -> "Speed must be between 0 and 1"
        "No direction allowed for reactive %0" -> "No direction allowed for reactive %0"
        "Grain with id %0 doesn't exist for reactive %1" -> "Agent with id %0 doesn't exist for reactive %1"
        "Half-life must be positive or zero" -> "Half-life must be positive or zero"
        "Field production for %0 must be between -1 and 1" -> "Field production for %0 must be between -1 and 1"
        "Field permeability will prevent production for %0" -> "Field permeability will prevent production for %0"
        "Field influence for %0 must be between -1 and 1" -> "Field influence for %0 must be between -1 and 1"
        "Field permeability for %0 must be between 0 and 1" -> "Field permeability for %0 must be between 0 and 1"
        "Field threshold value for %0 must be between 0 and 1" -> "Field threshold value for %0 must be between 0 and 1"

        "Previous" -> "Previous"
        "Next" -> "Next"
        "Ok" -> "Ok"

        "Anonymous" -> "Anonymous"
        "Logout" -> "Logout"
        "Log In" -> "Log In"
        "Register" -> "Register"

        "Examples" -> "Examples"
        "Home" -> "Home"
        "Show" -> "Show"
        "Subscribe" -> "Subscribe"
        "Administration" -> "Administration"

        "Documentation" -> "Documentation"
        "Contact Us" -> "Contact Us"

        "version of %0" -> "version of %0"

        "all" -> "all"
        "Model" -> "Model"
        "Models" -> "Models"
        "models" -> "models"
        "Simulation" -> "Simulation"
        "Simulations" -> "Simulations"
        "simulations" -> "simulations"
        "Environment" -> "Environment"

        "by %0" -> "by %0"
        "by me" -> "by me"
        "New Model" -> "New Model"
        "Model Name" -> "Model Name"
        "Simulation Name" -> "Simulation Name"
        "Description" -> "Description"
        "Model Description" -> "Model Description"
        "Simulation Description" -> "Simulation Description"
        "Clone Model" -> "Clone Model"
        "Clone Simulation" -> "Clone Simulation"
        "Import" -> "Load"
        "Export" -> "Save"
        "Expert Mode" -> "Expert Mode"
        "Log-in to save" -> "Log-in to save"
        "Delete Model" -> "Delete Model"
        "Delete Simulation" -> "Delete Simulation"
        "New Simulation" -> "New Simulation"
        "Save state as thumbnail" -> "Save state as thumbnail"
        "Download screenshot" -> "Download screenshot"
        "Download Model" -> "Download Model"
        "Download Simulation" -> "Download Simulation"
        "Loading simulations" -> "Loading simulations"
        "Model %0 saved." -> "Model %0 saved."
        "Simulation %0 saved." -> "Simulation %0 saved."
        "Updated speed for %0 to %1." -> "Updated speed for %0 to %1."
        "Current state saved as thumbnail." -> "Current state saved as thumbnail."
        "Model %0 and simulation %1 saved." -> "Model %0 and simulation %1 saved."
        "Model and simulation cloned." -> "Model and simulation cloned."
        "New simulation." -> "New simulation."
        "Simulation cloned." -> "Simulation cloned."
        "Model %0 deleted." -> "Model %0 deleted."
        "Load new simulation. Are you sure ?" -> "Load new simulation. Are you sure ?"
        "You're about to delete the simulation '%0'." -> "You're about to delete the simulation '%0'."
        "Delete model. Are you sure ?" -> "Delete model. Are you sure ?"
        "You're about to delete the model '%0' and its simulations." -> "You're about to delete the model '%0' and its simulations."
        "This action can't be undone." -> "This action can't be undone."
        "Simulation %0 deleted." -> "Simulation %0 deleted"
        "Modifications not saved. Do you wan't to save ?" -> "Modifications not saved. Do you wan't to save ?"
        "You're about to quit the page and some modifications haven't been saved." -> "You're about to quit the page and some modifications haven't been saved."
        "Yes" -> "Yes"
        "No" -> "No"
        "Don't save" -> "Don't save"
        "Stay here" -> "Stay here"

        "My models and simulations" -> "My models and simulations"
        "My Recent simulation" -> "My Recent simulation"

        "Search" -> "Search"
        "Searching" -> "Searching"
        "No simulation found" -> "No simulation found"
        "No model found" -> "No model found"
        "Recent simulations" -> "Recent simulations"

        "Public models" -> "Public models"
        "Featured" -> "Featured"
        "Monitoring" -> "Monitoring"
        "Users" -> "Users"
        "Asset" -> "Asset"
        "Assets" -> "Assets"
        "Send" -> "Send"
        "Asset created with id %0." -> "Asset created with id %0."

        "New tag" -> "New tag"
        "Tags" -> "Tags"
        "Popular" -> "Popular"

        "Select a element to edit it" -> "Select a element to edit it"
        "Message" -> "Message"
        "Source" -> "Source"
        "Field" -> "Field"
        "Fields" -> "Fields"
        "Grain" -> "Agent"
        "Grains" -> "Agents"
        "Behaviour" -> "Behaviour"
        "Behaviours" -> "Behaviours"
        "Name" -> "Name"
        "Display" -> "Display"
        "Invisible" -> "Invisible"
        "Speed" -> "Speed"
        "Half-life" -> "Half-life"
        "Movement" -> "Movement"
        "Productions" -> "Productions"
        "Influences" -> "Influences"
        "Permeability" -> "Permeability"
        "Size" -> "Size"
        "Reactives" -> "Reactives"
        "Directions" -> "Directions"
        "Products" -> "Products"
        "Sources" -> "With age from"
        "none" -> "none"
        "reactive %0" -> "reactive %0"
        "Field thresholds" -> "Field thresholds"
        "Field influences" -> "Field influences"
        "When age" -> "When age"
        "Age" -> "Age"
        "Reactions" -> "Reactions"
        "Url" -> "Url"
        "Position (x,y,z)" -> "Position (x,y,z)"
        "Position x" -> "Position x"
        "Position y" -> "Position y"
        "Position z" -> "Position z"
        "Scale (x,y,z)" -> "Scale (x,y,z)"
        "Scale x" -> "Scale x"
        "Scale y" -> "Scale y"
        "Scale z" -> "Scale z"
        "Rotation (x,y,z)" -> "Rotation (x,y,z)"
        "Rotation x" -> "Rotation x"
        "Rotation y" -> "Rotation y"
        "Rotation z" -> "Rotation z"
        "Fine" -> "Fine"
        "Small" -> "Small"
        "Medium" -> "Medium"
        "Large" -> "Large"
        "Formula" -> "Formula"
        "Formula Fields" -> "Formula Fields"
        "Formula Parameters" -> "Formula Parameters"
        "Current simulation step" -> "Current simulation step"
        "Slot x position" -> "Slot x position"
        "Slot y position" -> "Slot y position"
        "Current field value (value if no formula is provided)" -> "Current field value (value if no formula is provided)"
        "Model functions:" -> "Model function"
        "Operators and functions" -> "Operators and functions"
        "Mathematical operators" -> "Mathematical operators"
        "Modulo: returns the remainder of a division, after one number is divided by another" -> "Modulo: returns the remainder of a division, after one number is divided by another"
        "Exponentiation: a^b means a raised to the power of b" -> "Exponentiation: a^b means a raised to the power of b"
        "Logical 'and', 'or', 'not' operators" -> "Logical 'and', 'or', 'not' operators"
        "Equality operators" -> "Equality operators"
        "Comparison operators" -> "Comparison operators"
        "If else ternary operator" -> "If else ternary operator"
        "PI and E constants" -> "PI and E constants"
        "Absolute value" -> "Absolute value"
        "Average of n values" -> "Average of n values"
        "Trigonometry function including arc (acos, asin, atan) and hyperbolic (cosh, sinh, tanh)" -> "Trigonometry function including arc (acos, asin, atan) and hyperbolic (cosh, sinh, tanh)"
        "Floor, ceil and round functions" -> "Floor, ceil and round functions"
        "Logarithmic functions" -> "Logarithmic functions"
        "Min and max functions" -> "Min and max functions"
        "Summation function" -> "Summation function"

        "Step" -> "Step"
        "No line to show" -> "No line to show"

        "Loading" -> "Loading"

        "Total %0, this week %1, this month %2" -> "Total %0, this week %1, this month %2"

        "%0 must be a number" -> "%0 must be a number"
        "%0 must be between %1 and %2" -> "%0 must be between %1 and %2"

        "Simulation Settings" -> "Simulation Settings"
        "Background Color" -> "Background Color"
        "Grid" -> "Grid"
        "Image URL" -> "Image URL"

        "Tutorial" -> "Tutorial"
        "Tutorial '%0'" -> "Tutorial '%0'"
        "Create a simple bacterias simulation" -> "Create a simple bacterias simulation"
        "With this tutorial you will create a simulation bacterias division with only one grain and one behaviour." -> "With this tutorial you will create a simulation bacterias division with only one agent and one behaviour."
        "Create a bacteria grain" -> "Create a bacteria agent"
        "Click on " -> "Click on "
        " to add a grain to the simulation." -> " to add a agent to the simulation."
        "Change the name" -> "Change the name"
        "You can change the grain name, for instance to 'bact'." -> "You can change the agent name, for instance to 'bact'."
        "Set the speed" -> "Set the speed"
        "Change the speed to 1 to let the bacteria move." -> "Change the speed to 1 to let the bacteria move."
        "Go to simulation" -> "Go to simulation"
        "Open the simulation page to test the model." -> "Open the simulation page to test the model."
        "Draw some grains" -> "Draw some agents"
        "Draw %0 bacterias with the random spray." -> "Draw %0 bacterias with the random spray."
        "Run the simulation" -> "Run the simulation"
        "Watch the bacterias move." -> "Watch the bacterias move."
        "Stop the simulation" -> "Stop the simulation"
        "Ok, the bacterias moves." -> "Ok, the bacterias moves."
        "Go to model" -> "Go to model"
        "Open the model page to add a division behaviour to make them grow." -> "Open the model page to add a division behaviour to make them grow."
        "Create a division behaviour" -> "Create a division behaviour"
        " to add a behaviour to the simulation." -> " to add a behaviour to the simulation."
        "First product" -> "First product"
        "Select the bacteria grain as first product." -> "Select the bacteria agent as first product."
        "Adds a second product" -> "Adds a second product"
        " to add a second line in the behaviour" -> " to add a second line in the behaviour"
        "Second product" -> "Second product"
        "Select the bacteria grain as second product." -> "Select the bacteria agent as second product."
        "Return to simulation" -> "Return to simulation"
        "Go back to the simulation page." -> "Go back to the simulation page."
        "Watch the bacteria colony grow." -> "Watch the bacteria colony grow."
        "You've just created a simulation with Centyllion, well done 👍." -> "You've just created a simulation with Centyllion, well done 👍."
        "You can now for instance:" -> "You can now for instance:"
        "Set a half-life for bacterias to give them a life-span." -> "Set a half-life for bacterias to give them a life-span."
        "Add a sugar field to feed the bacterias." -> "Add a sugar field to feed the bacterias."
        "Create another bacteria to compete with." -> "Create another bacteria to compete with."
        "You can find some documentation here " -> "You can find some documentation here "
        "Start tutorial" -> "Start tutorial"
        "Ok but later" -> "Ok but later"
        "I don't need it" -> "I don't need it"

        "With this tutorial you will add to the simulation a field to feed the bacterias." -> "With this tutorial you will add to the simulation a field to feed the bacterias."
        "Open the model page to add a field." -> "Open the model page to add a field."
        "Create a sugar field" -> "Create a sugar field"
        " to add a field to the simulation." -> " to add a field to the simulation."
        "You can change the field name, for instance to 'sugar'." -> "You can change the field name, for instance to 'sugar'."
        "Create a source grain" -> "Create a source agent"
        " to add another grain to produce the 'sugar' field." -> " to add another agent to produce the 'sugar' field."
        "Produce 'sugar'" -> "Produce 'sugar'"
        "Set the production of 'sugar' field to 1." -> "Set the production of 'sugar' field to 1."
        "Select the bacteria grain" -> "Select the bacteria agent"
        "Make the bacterias attracted to sugar." -> "Make the bacterias attracted to sugar."
        "Influenced by 'sugar'" -> "Influenced by 'sugar'"
        "Set the influence of 'sugar' above to 0.5." -> "Set the influence of 'sugar' above to 0.5."
        "Select the behavior" -> "Select the behavior"
        "Let's constrain the division with the 'sugar' field." -> "Let's constrain the division with the 'sugar' field."
        "Adds a field threshold" -> "Adds a field threshold"
        "Add a field constrain predicate to limit the behaviour to be only executed when the field is present." -> "Add a field constrain predicate to limit the behaviour to be only executed when the field is present."
        "Sets the threshold to 0.01" -> "Sets the threshold to 0.01"
        "The field value around a grain that produces it diminishes rapidly." -> "The field value around a agent that produces it diminishes rapidly."
        "Select the source grain" -> "Select the source agent"
        "Let's add some sources." -> "Let's add some sources."
        "Draw some source" -> "Draw some source"
        "Draw %0 source with the random spray." -> "Draw %0 source with the random spray."
        "Watch the bacteria colony grow around the sugar sources." -> "Watch the bacteria colony grow around the sugar sources."
        "Now you know how to use fields with Centyllion, well done 👍." -> "Now you know how to use fields with Centyllion, well done 👍."
        "Change the field threshold for sugar division (try 0.001 or 1e-6 (0.000001)." -> "Change the field threshold for sugar division (try 0.001 or 1e-6 (0.000001)."
        "Prevents the sources from moving." -> "Prevents the sources from moving."
        "Makes the bacterias consume the sugar (production to -0.5)." -> "Makes the bacterias consume the sugar (production to -0.5)."
        "Feed bacterias with sugar" -> "Feed bacterias with sugar"
        else -> key
    }
}