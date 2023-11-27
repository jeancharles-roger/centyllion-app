package com.centyllion.i18n

object French: Locale {
    override val name: String = "fr-FR"

    override val label: String = "Français"

    override fun value(key: String): String? = when (key) {
        "Grain with id %0 doesn't exist" -> "L'agent avec l'id %0 n'existe pas"
        "Behaviour must have a main reactive" -> "Le comportement doit avoir un réactif principal"
        "Probability must be between 0 and 1" -> "La vitesse doit être entre 0 et 1"
        "No direction allowed for reactive %0" -> "Aucune direction autorisée pour le réactif %0"
        "Grain with id %0 doesn't exist for reactive %1" -> "L'agent avec l'id %0 n'existe pas pour le réactif %1"
        "Half-life must be positive or zero" -> "La demi-vie doit être positive ou zéro"
        "Field production for %0 must be between -1 and 1" -> "La production du champ %0 doit être entre -1 et 1"
        "Field permeability will prevent production for %0" -> "La perméabilité du champ %0 empêche sa production"
        "Field influence for %0 must be between -1 and 1" -> "L'influence du champ %0 doit être entre -1 et 1"
        "Field permeability for %0 must be between 0 and 1" -> "La perméabilité du champ %0 doit être entre 0 et 1"
        "Field threshold value for %0 must be between 0 and 1" -> "Le seuil de champ pour %0 doit être entre 0 et 1"

        "Previous" -> "Précedent"
        "Next" -> "Suivant"
        "Ok" -> "Ok"

        "Anonymous" -> "Anonyme"
        "Logout" -> "Se déconnecter"
        "Log In" -> "Se connecter"
        "Register" -> "Créer un compte"

        "Explore" -> "Explorer"
        "Home" -> "Ma page"
        "Show" -> "Présenter"
        "Subscribe" -> "S'abonner"
        "Administration" -> "Administration"

        "Documentation" -> "Documentation"
        "Contact Us" -> "Contactez-nous"

        "version of %0" -> "version du %0"

        "all" -> "tout"
        "Model" -> "Modèle"
        "Models" -> "Modèles"
        "models" -> "modèles"
        "Simulation" -> "Simulation"
        "Simulations" -> "Simulations"
        "simulations" -> "simulations"
        "Environment" -> "Environnement"

        "by %0" -> "par %0"
        "by me" -> "à moi"
        "New Model" -> "Nouveau Modèle"
        "Model Name" -> "Nom du Modèle"
        "Simulation Name" -> "Nom de la Simulation"
        "Description" -> "Description"
        "Model Description" -> "Description du Modèle"
        "Simulation Description" -> "Description de la Simulation"
        "Clone Model" -> "Cloner le Modèle"
        "Clone Simulation" -> "Cloner la Simulation"
        "Import" -> "Charger"
        "Export" -> "Sauver"
        "Expert Mode" -> "Mode Expert"
        "Log-in to save" -> "Connectez-vous pour sauver"
        "Delete Model" -> "Supprimer le Modèle"
        "Delete Simulation" -> "Supprimer la Simulation"
        "New Simulation" -> "Nouvelle Simulation"
        "Save state as thumbnail" -> "Enregistrer comme miniature"
        "Download screenshot" -> "Télécharger une capture"
        "Download Model" -> "Télécharger le Modèle"
        "Download Simulation" -> "Télécharger la Simulation"
        "Loading simulations" -> "Chargement des simulations"
        "Model %0 saved." -> "Modèle %0 enregistré."
        "Simulation %0 saved." -> "Simulation %0 enregistré."
        "Updated speed for %0 to %1." -> "Mise à jour de la vitesse de %0 à %1."
        "Updated speed for %0 to %1." -> "Mise à jour de la vitesse de %0 à %1."
        "Current state saved as thumbnail." -> "État courant enregistré comme miniature."
        "Model %0 and simulation %1 saved." -> "Modèle %0 et simulation %1 enregistrés."
        "Model and simulation cloned." -> "Modèle et simulation clonés."
        "New simulation." -> "Nouvelle simulation."
        "Simulation cloned." -> "Simulation clonée."
        "Model %0 deleted." -> "Modèle %0 supprimé."
        "Delete simulation. Are you sure ?" -> "Supprimer la simulation. Êtes-vous sur ?"
        "You're about to delete the simulation '%0'." -> "Vous allez supprimer la simulation '%0'."
        "Delete model. Are you sure ?" -> "Supprimer le modèle. Êtes-vous sur ?"
        "You're about to delete the model '%0' and its simulations." -> "Vous allez supprimer le modèle '%0' et ses simulations."
        "This action can't be undone." -> "Cette action ne peut-être annulée."
        "Simulation %0 deleted." -> "La simulation %0 a été supprimée"
        "Modifications not saved. Do you wan't to save ?" -> "Des modifications n'ont pas été enregistrées. Voulez-vous enregistrer ?"
        "You're about to quit the page and some modifications haven't been saved." -> "Vous allez quitter la page mais des modifications n'ont pas été enregistrées."
        "Yes" -> "Oui"
        "No" -> "Non"
        "Don't save" -> "Ne pas enregistrer"
        "Stay here" -> "Rester sur la page"

        "My models and simulations" -> "Mes modèles et simulations"
        "My Recent simulation" -> "Mes simulations récentes"

        "Search" -> "Chercher"
        "No simulation found" -> "Aucune simulation trouvée"
        "No model found" -> "Aucun modèle trouvé"
        "Recent simulations" -> "Simulations récentes"
        "Featured" -> "Sélection de simulations"

        "Public models" -> "Modèles publics"
        "Monitoring" -> "Monitoring"
        "Users" -> "Utilisateurs"
        "Asset" -> "Ressource"
        "Assets" -> "Ressources"
        "Send" -> "Envoyer"
        "Asset created with id %0." -> "Ressource créée avec l'id %0."

        "New tag" -> "Ajouter un tag"
        "Tags" -> "Tags"
        "Popular" -> "Populaires"

        "Select a element to edit it" -> "Sélectionner un élément pour l'éditer"
        "Message" -> "Message"
        "Source" -> "Source"
        "Field" -> "Champ"
        "Fields" -> "Champs"
        "Grain" -> "Agent"
        "Grains" -> "Agents"
        "Behaviour" -> "Comportement"
        "Behaviours" -> "Comportements"
        "Name" -> "Nom"
        "Display" -> "Affichage"
        "Invisible" -> "Invisible"
        "Speed" -> "Vitesse"
        "Half-life" -> "Demi-vie"
        "Movement" -> "Déplacement"
        "Productions" -> "Productions"
        "Influences" -> "Influences"
        "Permeability" -> "Perméabilités"
        "Size" -> "Taille"
        "Reactives" -> "Réactifs"
        "Directions" -> "Directions"
        "Products" -> "Produits"
        "Sources" -> "Avec l'âge de"
        "none" -> "aucun"
        "reactive %0" -> "réactif %0"
        "Field thresholds" -> "Seuils des champs"
        "Field influences" -> "Influences des champs"
        "When age" -> "Avec l'âge"
        "Age" -> "Âge"
        "Reactions" -> "Réactions"
        "Url" -> "Url"
        "Position (x,y,z)" -> "Position (x,y,z)"
        "Position x" -> "Position x"
        "Position y" -> "Position y"
        "Position z" -> "Position z"
        "Scale (x,y,z)" -> "Échelle (x,y,z)"
        "Scale x" -> "Échelle x"
        "Scale y" -> "Échelle y"
        "Scale z" -> "Échelle z"
        "Rotation (x,y,z)" -> "Rotation (x,y,z)"
        "Rotation x" -> "Rotation x"
        "Rotation y" -> "Rotation y"
        "Rotation z" -> "Rotation z"
        "Fine" -> "Fin"
        "Small" -> "Petit"
        "Medium" -> "Moyen"
        "Large" -> "Grand"
        "Formula" -> "Formule"
        "Formula Fields" -> "Champs calculés"
        "Formula Parameters" -> "Paramètres de la formule"
        "Current simulation step" -> "Étape de simulation courante"
        "Slot x position" -> "Position en x de la case courante"
        "Slot y position" -> "Position en y de la case courante"
        "Current field value (value if no formula is provided)" -> "Valeur du champ courant (valeur si aucune formule n'est donnée)"
        "Model functions:" -> "Fonctions sur le modèle"
        "Operators and functions" -> "Opérateurs et fonctions"
        "Mathematical operators" -> "Opérateurs mathematiques"
        "Modulo: returns the remainder of a division, after one number is divided by another" -> "Modulo: retourne le reste de la division entière"
        "Exponentiation: a^b means a raised to the power of b" -> "Exponentielle: a^b est a puissance b"
        "Logical 'and', 'or', 'not' operators" -> "Opérateurs logiques 'and', 'or', 'not'"
        "Equality operators" -> "Opérateurs d'égalités"
        "Comparison operators" -> "Opérateurs de comparaisons"
        "If else ternary operator" -> "If else operateur ternaire"
        "PI and E constants" -> "Constantes PI et E"
        "Absolute value" -> "Valeur absolue"
        "Average of n values" -> "Moyenne de n valeurs"
        "Trigonometry function including arc (acos, asin, atan) and hyperbolic (cosh, sinh, tanh)" -> "Fonctions trigonometriques avec arc (acos, asin, atan) et hyperbolique (cosh, sinh, tanh)"
        "Floor, ceil and round functions" -> "Fonctions d'arrondis floor, ceil et round"
        "Logarithmic functions" -> "Fonctions logarithmiques"
        "Min and max functions" -> "Fonctions min et max"
        "Summation function" -> "Fonctions de somme"

        "Step" -> "Pas"
        "No line to show" -> "Aucune ligne à montrer"

        "Loading" -> "Chargement"

        "Total %0, this week %1, this month %2" -> "Total %0, cette semaine %1, ce mois %2"

        "%0 must be a number" -> "%0 doit être un nombre"
        "%0 must be between %1 and %2" -> "%0 doit être entre %1 et %2"

        "Simulation Settings" -> "Paramètres de simulation"
        "Background Color" -> "Couleur du fond"
        "Grid" -> "Grille"
        "Image URL" -> "URL de l'image"

        "Tutorial" -> "Tutoriel"
        "Tutorial '%0'" -> "Tutoriel '%0'"
        "Create a simple bacterias simulation" -> "Créer une simulation simple de bactéries"
        "With this tutorial you will create a simulation bacterias division with only one grain and one behaviour." -> "Avec ce tutoriel, vous allez créer une simulation de division de bactéries avec un agent et un comportement."
        "Create a bacteria grain" -> "Créer un agent de bacterie"
        "Click on " -> "Cliquez sur "
        " to add a grain to the simulation." -> " pour ajouter un agent à la simulation."
        "Change the name" -> "Changer le nom"
        "You can change the grain name, for instance to 'bact'." -> "Vous pouvez changer le nom, par exemple en 'bact'."
        "Set the speed" -> "Changer la vitesse"
        "Change the speed to 1 to let the bacteria move." -> "Changer la vitesse à 1 pour que la bactérie se déplace."
        "Go to simulation" -> "Aller sur simulation"
        "Open the simulation page to test the model." -> "Ouvrez la page de simulation pour tester le modèle."
        "Draw some grains" -> "Dessiner quelques agents"
        "Draw %0 bacterias with the random spray." -> "Dessinez %0 bacteries avec le l'outil aléatoire."
        "Run the simulation" -> "Lancer la simulation"
        "Watch the bacterias move." -> "Regardez les bactéries se déplacer."
        "Stop the simulation" -> "Arreter la simulation"
        "Ok, the bacterias moves." -> "Bon, les bacteries se déplacent."
        "Go to model" -> "Aller sur le modèle"
        "Open the model page to add a division behaviour to make them grow." -> "Ouvrez la page de modèle pour ajouter un comportement de division pour que les bactéries se développent."
        "Create a division behaviour" -> "Créer un comportement de division"
        " to add a behaviour to the simulation." -> " pour ajouter un comportement à la simulation."
        "First product" -> "Premier produit"
        "Select the bacteria grain as first product." -> "Sélectionnez la bacterie comme premier produit."
        "Adds a second product" -> "Ajouter un second produit"
        " to add a second line in the behaviour" -> " pour ajouter une seconde ligne dans le comportement"
        "Second product" -> "Second produit"
        "Select the bacteria grain as second product." -> "Sélectionnez la bacterie comme second produit."
        "Return to simulation" -> "Retourner à la simulation"
        "Go back to the simulation page." -> "Retournez à la page de simulation."
        "Watch the bacteria colony grow." -> "Regardez la colonie de bactéries se développer."
        "You've just created a simulation with Centyllion, well done 👍." -> "Vous venez de créer une simulation avec Centyllion, bravo 👍."
        "You can now for instance:" -> "Vous pouvez maintenant:"
        "Set a half-life for bacterias to give them a life-span." -> "Donnez une demi-vie aux bacteries afin de limiter leur durée de vie."
        "Add a sugar field to feed the bacterias." -> "Ajoutez un champ de sucre pour nourrir les bactéries."
        "Create another bacteria to compete with." -> "Créez une autre bactérie compétitive."
        "You can find some documentation here " -> "La documentation est disponible ici "
        "Start tutorial" -> "Démarrer le tutoriel"
        "Ok but later" -> "Ok mais plus tard"
        "I don't need it" -> "Je n'ai pas besoin"

        "With this tutorial you will add to the simulation a field to feed the bacterias." -> "Avec ce tutoriel, vous allez ajouter un champ à la simulation pour nourrir les bacteries."
        "Open the model page to add a field." -> "Ouvrez le modèle pour ajouter un champ."
        "Create a sugar field" -> "Créer une champ de sucre"
        " to add a field to the simulation." -> " pour ajouter un champ à la simulation."
        "You can change the field name, for instance to 'sugar'." -> "Vous pouvez changer le nom du champ, par exemple: 'sucre'."
        "Create a source grain" -> "Créer un agent source"
        " to add another grain to produce the 'sugar' field." -> " pour ajouter un autre agent pour produire le champ 'sucre'."
        "Produce 'sugar'" -> "Produire du 'sucre'"
        "Set the production of 'sugar' field to 1." -> "Mettez la production du champ 'sucre' à 1."
        "Select the bacteria grain" -> "Sélectionner l'agent 'bactérie'"
        "Make the bacterias attracted to sugar." -> "Rendez les bactéries attirées par 'sucre'."
        "Influenced by 'sugar'" -> "Influencés par 'sucre'"
        "Set the influence of 'sugar' above to 0.5." -> "Mettez l'influence du 'sucre' au dessus de 0.5."
        "Select the behavior" -> "Sélectionner le comportement"
        "Let's constrain the division with the 'sugar' field." -> "Contraignez la division avec le champ 'sucre'."
        "Adds a field threshold" -> "Ajouter un seuil de champ"
        "Add a field constrain predicate to limit the behaviour to be only executed when the field is present." -> "Ajoutez un prédicat de contrainte pour limiter le comportement que lorsque le champ est présent."
        "Sets the threshold to 0.01" -> "Mettre le seuil à 0.01"
        "The field value around a grain that produces it diminishes rapidly." -> "La valeur du champ autour du agent source diminue rapidement."
        "Select the source grain" -> "Sélectionner l'agent source"
        "Let's add some sources." -> "Ajoutez des sources de 'sucre'."
        "Draw some source" -> "Dessiner des sources"
        "Draw %0 source with the random spray." -> "Dessinez %0 source avec le l'outil aléatoire."
        "Watch the bacteria colony grow around the sugar sources." -> "Regardez les bactéries se développer autour des sources de sucre."
        "Now you know how to use fields with Centyllion, well done 👍." -> "Maintenant vous savez utiliser les champs avec Centyllion, bravo 👍."
        "Change the field threshold for sugar division (try 0.001 or 1e-6 (0.000001)." -> "Changer le seuil du champ sucre pour la division (essayez 0.001 ou 1e-6 (0.000001)."
        "Prevents the sources from moving." -> "Empécher les sources de sucre de bouger."
        "Makes the bacterias consume the sugar (production to -0.5)." -> "Rendre les bacterias consommatrice de sucre (production à -0.5)."
        "Feed bacterias with sugar" -> "Du sucre pour les bactéries"
        else -> null
    }
}