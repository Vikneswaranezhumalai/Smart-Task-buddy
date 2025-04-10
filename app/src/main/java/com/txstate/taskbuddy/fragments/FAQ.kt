class FAQFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_faq, container, false)

        // You can customize this with real FAQ data or use a RecyclerView
        val faqTextView = view.findViewById<TextView>(R.id.faqTextView)
        faqTextView.text = """
            Q: How do I add a task?
            A: Tap the '+' button on the Task List screen.

            Q: How can I reset my password?
            A: Go to the Login screen and tap "Forgot Password".

            Q: Where can I see completed tasks?
            A: Check the Task History screen.
        """.trimIndent()

        return view
    }
}
